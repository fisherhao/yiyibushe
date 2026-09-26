package com.dayu.yiyibushe.app.ai.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.constant.AiConstants;
import com.dayu.yiyibushe.infra.ai.prompt.PromptStore;
import com.dayu.yiyibushe.infra.ai.trace.AgentToolTraceSupport;
import com.dayu.yiyibushe.infra.ai.trace.ExecutionTrace;
import com.dayu.yiyibushe.infra.ai.trace.TracePhase;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工具 2：天气查询（function calling，非 MCP）。
 * <p>
 * 入参二选一：{@code city}（城市名）或 {@code latitude + longitude}（经纬度，
 * 通常来自 {@link GetCurrentLocationTool}）。
 * 数据源：开源免费 Open-Meteo（主源，含城市名→经纬度 geocoding）；
 * 主源任何环节失败自动兜底开源免费 wttr.in，全程无需 API Key。
 * 调用计数 {@link #getCallCount()} 供测试验证"是否被模型命中"。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class GetWeatherTool implements AgentTool {

    private static final Logger log = LogUtilExt.getLogger(GetWeatherTool.class);

    /** 工具名（模型 function calling 选择用，需唯一） */
    private static final String TOOL_NAME = "get-weather";

    /** 所属技能名（对应 current-weather/SKILL.md） */
    private static final String SKILL_NAME = "current-weather";

    /** Open-Meteo 城市名 → 经纬度（开源免费 geocoding，中文） */
    private static final String OPEN_METEO_GEOCODING_API =
            "https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1&language=zh&format=json";

    /** Open-Meteo 实时天气（开源免费，无需 key；timezone 直接写斜杠，由 HTTP 客户端按 query 原样发送） */
    private static final String OPEN_METEO_FORECAST_API =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}"
                    + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
                    + "&timezone=Asia/Shanghai";

    /** wttr.in 兜底：{place} 可为城市名或 "纬度,经度" */
    private static final String WTTR_API = "https://wttr.in/{place}?format=j1&lang=zh";

    /** 命中次数：测试用于验证对话是否触发了本工具 */
    private final AtomicInteger callCount = new AtomicInteger();

    /** 共享 HTTP 客户端（RestClientConfig 统一注入） */
    @Autowired
    private RestClient restClient;

    /** 提示词存储：工具描述、参数描述、结果模板与错误文案从此读取 */
    @Autowired
    private PromptStore promptStore;

    /**
     * 工具名称
     *
     * @return 工具名称
     */
    @Override
    public String getName() {
        return TOOL_NAME;
    }

    /**
     * 工具描述：供模型判断何时调用、如何传参
     *
     * @return 工具描述
     */
    @Override
    public String getDescription() {
        return promptStore.require(AiConstants.PROMPT_WEATHER_TOOL_DESC);
    }

    /**
     * 入参 JSON Schema：city 或 latitude+longitude（均非强制 required，由模型按场景二选一）
     *
     * @return 参数 schema
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> cityProperty = new LinkedHashMap<>();
        cityProperty.put("type", "string");
        cityProperty.put("description", promptStore.require(AiConstants.PROMPT_WEATHER_ARG_CITY_DESC));

        Map<String, Object> latProperty = new LinkedHashMap<>();
        latProperty.put("type", "number");
        latProperty.put("description", promptStore.require(AiConstants.PROMPT_WEATHER_ARG_LAT_DESC));

        Map<String, Object> lonProperty = new LinkedHashMap<>();
        lonProperty.put("type", "number");
        lonProperty.put("description", promptStore.require(AiConstants.PROMPT_WEATHER_ARG_LON_DESC));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("city", cityProperty);
        properties.put("latitude", latProperty);
        properties.put("longitude", lonProperty);

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", List.of());
        return parameters;
    }

    /**
     * 执行天气查询：主源 Open-Meteo（city 先 geocoding）→ 失败兜底 wttr.in
     *
     * @param param
     *     调用参数（city 或 latitude/longitude）
     * @return 天气文本或错误块
     */
    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        callCount.incrementAndGet();
        ExecutionTrace trace = AgentToolTraceSupport.resolve(param);
        // 懒加载闸门：技能正文未加载时拒绝执行，强制模型先调 load-skill-instructions
        if (Objects.nonNull(trace) && !trace.isSkillInstructionsLoaded(SKILL_NAME)) {
            trace.step(TracePhase.TOOL,
                    "懒加载闸门拦截：技能【{0}】正文尚未加载，{1} 暂不执行，要求模型先加载指令",
                    SKILL_NAME, TOOL_NAME);
            return Mono.just(ToolResultBlock.error(
                    promptStore.format(AiConstants.PROMPT_SKILL_GATE_BLOCKED, SKILL_NAME, TOOL_NAME)));
        }
        long startMillis = System.currentTimeMillis();
        Map<String, Object> input = param.getInput();
        String city = asString(input.get("city"));
        Double latitude = asDouble(input.get("latitude"));
        Double longitude = asDouble(input.get("longitude"));

        if (Objects.nonNull(trace)) {
            trace.hitSkill(SKILL_NAME);
            trace.hitTool(TOOL_NAME);
            trace.step(TracePhase.TOOL,
                    "模型决策命中工具 {0}，入参：city={1}，latitude={2}，longitude={3}",
                    TOOL_NAME, displayValue(city), displayValue(latitude), displayValue(longitude));
        }

        String result;
        if (Objects.nonNull(latitude) && Objects.nonNull(longitude)) {
            // 经纬度模式（当前位置链路）
            result = queryByCoordinates(StringUtilExt.defaultIfBlank(city,
                    promptStore.require(AiConstants.PROMPT_WEATHER_LOCATION_DEFAULT)),
                    latitude, longitude, trace);
        } else if (StringUtilExt.isNotBlank(city)) {
            // 城市模式
            result = queryByCity(StringUtilExt.trim(city), trace);
        } else {
            return Mono.just(ToolResultBlock.error(
                    promptStore.require(AiConstants.PROMPT_WEATHER_ARG_MISSING)));
        }

        long elapsedMillis = System.currentTimeMillis() - startMillis;
        if (StringUtilExt.isNotBlank(result)) {
            LogUtilExt.info(log, "[WeatherTool] 查询完成: {0}", result);
            if (Objects.nonNull(trace)) {
                trace.stepWithDuration(TracePhase.TOOL, elapsedMillis,
                        "天气查询完成，结构化结果回传模型");
            }
            return Mono.just(ToolResultBlock.text(result));
        }
        if (Objects.nonNull(trace)) {
            trace.stepWithDuration(TracePhase.TOOL, elapsedMillis,
                    "主源 Open-Meteo 与兜底 wttr.in 均不可用");
        }
        return Mono.just(ToolResultBlock.error(
                promptStore.require(AiConstants.PROMPT_WEATHER_SOURCE_FAIL)));
    }

    /**
     * 城市模式：Open-Meteo geocoding 转经纬度后查主源；任一步失败回退 wttr.in 城市接口
     *
     * @param city
     *     城市名
     * @param trace
     *     执行轨迹（可空）
     * @return 天气文本，全部失败返回 null
     */
    private String queryByCity(String city, ExecutionTrace trace) {
        try {
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "城市模式：先调 Open-Meteo geocoding 解析城市经纬度");
            }
            String geoBody = restClient.get()
                    .uri(OPEN_METEO_GEOCODING_API, city)
                    .retrieve()
                    .body(String.class);
            JSONArray searchResults = JSON.parseObject(geoBody).getJSONArray("results");
            if (CollectionUtilExt.isNotEmpty(searchResults)) {
                JSONObject location = searchResults.getJSONObject(0);
                double resolvedLat = location.getDoubleValue("latitude");
                double resolvedLon = location.getDoubleValue("longitude");
                String resolvedName = StringUtilExt.defaultIfBlank(location.getString("name"), city);
                String primary = formatOpenMeteo(resolvedName, resolvedLat, resolvedLon, trace);
                if (StringUtilExt.isNotBlank(primary)) {
                    return primary;
                }
            }
        } catch (RuntimeException e) {
            LogUtilExt.warn(log, "[WeatherTool] Open-Meteo 城市解析失败，切换 wttr.in: {0}", e.getMessage());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "Open-Meteo geocoding 异常，切换兜底源 wttr.in：{0}",
                        e.getMessage());
            }
        }
        return formatWttr(city, city, trace);
    }

    /**
     * 经纬度模式：直接查 Open-Meteo 主源；失败回退 wttr.in 坐标接口
     *
     * @param locationName
     *     位置展示名
     * @param latitude
     *     纬度
     * @param longitude
     *     经度
     * @param trace
     *     执行轨迹（可空）
     * @return 天气文本，全部失败返回 null
     */
    private String queryByCoordinates(String locationName, double latitude, double longitude,
            ExecutionTrace trace) {
        String primary = formatOpenMeteo(locationName, latitude, longitude, trace);
        if (StringUtilExt.isNotBlank(primary)) {
            return primary;
        }
        // wttr.in 支持 "纬度,经度" 作为地点
        if (Objects.nonNull(trace)) {
            trace.step(TracePhase.TOOL, "主源失败，切换兜底源 wttr.in（坐标模式）");
        }
        String place = latitude + "," + longitude;
        return formatWttr(locationName, place, trace);
    }

    /**
     * Open-Meteo 主源查询并格式化
     *
     * @param locationName
     *     位置展示名
     * @param latitude
     *     纬度
     * @param longitude
     *     经度
     * @param trace
     *     执行轨迹（可空）
     * @return 天气文本，失败返回 null
     */
    private String formatOpenMeteo(String locationName, double latitude, double longitude,
            ExecutionTrace trace) {
        try {
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "请求主源 Open-Meteo 实时天气接口（开源免费，8秒超时）");
            }
            String body = restClient.get()
                    .uri(OPEN_METEO_FORECAST_API, latitude, longitude)
                    .retrieve()
                    .body(String.class);
            JSONObject current = JSON.parseObject(body).getJSONObject("current");
            int weatherCode = current.getIntValue("weather_code");
            return promptStore.format(AiConstants.PROMPT_WEATHER_RESULT_METEO,
                    locationName,
                    describeWmoCode(weatherCode),
                    current.getBigDecimal("temperature_2m"),
                    current.getBigDecimal("relative_humidity_2m"),
                    current.getBigDecimal("wind_speed_10m"),
                    current.getString("time"));
        } catch (RuntimeException e) {
            LogUtilExt.warn(log, "[WeatherTool] Open-Meteo 查询失败: {0}", e.getMessage());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "Open-Meteo 实时天气查询失败：{0}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * wttr.in 兜底查询并格式化
     *
     * @param locationName
     *     位置展示名
     * @param place
     *     wttr.in 地点（城市名或 纬度,经度）
     * @param trace
     *     执行轨迹（可空）
     * @return 天气文本，失败返回 null
     */
    private String formatWttr(String locationName, String place, ExecutionTrace trace) {
        try {
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "请求兜底源 wttr.in（开源免费，8秒超时）");
            }
            String body = restClient.get().uri(WTTR_API, place).retrieve().body(String.class);
            JSONObject condition = JSON.parseObject(body)
                    .getJSONArray("current_condition")
                    .getJSONObject(0);
            // lang=zh 时取中文描述，取不到回退英文
            JSONArray zhDescriptions = condition.getJSONArray("lang_zh");
            String weatherText = Objects.nonNull(zhDescriptions)
                    ? zhDescriptions.getJSONObject(0).getString("value")
                    : condition.getJSONArray("weatherDesc").getJSONObject(0).getString("value");
            return promptStore.format(AiConstants.PROMPT_WEATHER_RESULT_WTTR,
                    locationName,
                    weatherText,
                    condition.getString("temp_C"),
                    condition.getString("FeelsLikeC"),
                    condition.getString("humidity"),
                    condition.getString("windspeedKmph"));
        } catch (RuntimeException e) {
            LogUtilExt.warn(log, "[WeatherTool] wttr.in 查询也失败: {0}", e.getMessage());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "wttr.in 查询失败：{0}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * WMO 天气代码转中文描述（Open-Meteo 使用 WMO Weather interpretation codes）
     *
     * @param code
     *     WMO 代码
     * @return 中文天气现象
     */
    private String describeWmoCode(int code) {
        return switch (code) {
            case 0 -> "晴";
            case 1 -> "大部晴朗";
            case 2 -> "多云";
            case 3 -> "阴";
            case 45, 48 -> "雾";
            case 51, 53, 55 -> "毛毛雨";
            case 56, 57 -> "冻毛毛雨";
            case 61 -> "小雨";
            case 63 -> "中雨";
            case 65 -> "大雨";
            case 66, 67 -> "冻雨";
            case 71 -> "小雪";
            case 73 -> "中雪";
            case 75 -> "大雪";
            case 77 -> "雪粒";
            case 80, 81, 82 -> "阵雨";
            case 85, 86 -> "阵雪";
            case 95 -> "雷暴";
            case 96, 99 -> "雷暴伴冰雹";
            default -> promptStore.format(AiConstants.PROMPT_WEATHER_WMO_UNKNOWN, code);
        };
    }

    /**
     * 入参转字符串
     *
     * @param value
     *     入参值
     * @return 字符串或 null
     */
    private String asString(Object value) {
        return Objects.isNull(value) ? null : String.valueOf(value);
    }

    /**
     * 入参转 Double
     *
     * @param value
     *     入参值
     * @return Double 或 null
     */
    private Double asDouble(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.valueOf(String.valueOf(value));
    }

    /**
     * trace 展示用：null 显示为空值标记，其余原样
     *
     * @param value
     *     入参值
     * @return 展示文本
     */
    private String displayValue(Object value) {
        return Objects.isNull(value) ? "未传" : String.valueOf(value);
    }

    /**
     * 获取本工具被模型命中的次数
     *
     * @return 命中次数
     */
    public int getCallCount() {
        return callCount.get();
    }
}
