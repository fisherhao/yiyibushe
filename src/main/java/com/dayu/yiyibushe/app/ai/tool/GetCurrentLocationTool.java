package com.dayu.yiyibushe.app.ai.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
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
 * 工具 1：获取当前位置（function calling，非 MCP）。
 * <p>
 * 按网络出口 IP 调开源免费 ip-api 定位，返回城市名与经纬度，供 {@link GetWeatherTool}
 * 继续查询。调用计数 {@link #getCallCount()} 供测试验证"是否被模型命中"。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class GetCurrentLocationTool implements AgentTool {

    private static final Logger log = LogUtilExt.getLogger(GetCurrentLocationTool.class);

    /** 工具名（模型 function calling 选择用，需唯一） */
    private static final String TOOL_NAME = "get-current-location";

    /** 所属技能名（对应 current-weather/SKILL.md） */
    private static final String SKILL_NAME = "current-weather";

    /** 定位接口（开源免费，HTTP 免费版，无需 key；中文返回） */
    private static final String IP_LOCATION_API =
            "http://ip-api.com/json/?lang=zh-CN&fields=status,message,city,regionName,lat,lon,query";

    /** 命中次数：测试用于验证对话是否触发了本工具 */
    private final AtomicInteger callCount = new AtomicInteger();

    /** 共享 HTTP 客户端（RestClientConfig 统一注入） */
    @Autowired
    private RestClient restClient;

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
     * 工具描述：供模型判断何时调用
     *
     * @return 工具描述
     */
    @Override
    public String getDescription() {
        return "按当前网络出口 IP 获取当前所在位置，返回城市名、纬度、经度。无参数。"
                + "当用户询问当前位置天气但没有给出城市名时，先调用本工具，再用经纬度调用 get-weather。";
    }

    /**
     * 入参 JSON Schema：无参数
     *
     * @return 空参数 schema
     */
    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(),
                "required", List.of());
    }

    /**
     * 执行定位：成功返回结构化 JSON 文本（city/latitude/longitude/ip）
     *
     * @param param
     *     调用参数（本工具无入参，不使用）
     * @return 位置信息或错误块
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
            return Mono.just(ToolResultBlock.error("请先调用 load-skill-instructions（skillName="
                    + SKILL_NAME + "）加载技能指令，再重试 " + TOOL_NAME));
        }
        long startMillis = System.currentTimeMillis();
        if (Objects.nonNull(trace)) {
            trace.hitSkill(SKILL_NAME);
            trace.hitTool(TOOL_NAME);
            trace.step(TracePhase.TOOL, "模型决策命中工具 {0}（无入参），开始执行", TOOL_NAME);
            trace.step(TracePhase.TOOL, "发起外部 HTTP 请求：GET ip-api.com（开源免费，8秒超时）");
        }
        try {
            String body = restClient.get().uri(IP_LOCATION_API).retrieve().body(String.class);
            JSONObject json = JSON.parseObject(body);
            if (!StringUtilExt.equals("success", json.getString("status"))) {
                if (Objects.nonNull(trace)) {
                    trace.step(TracePhase.TOOL, "定位接口返回失败：{0}", json.getString("message"));
                }
                return Mono.just(ToolResultBlock.error("定位失败: " + json.getString("message")));
            }
            String city = StringUtilExt.defaultIfBlank(json.getString("city"), "未知");
            // 动态结果容器：lat/lon/query 字段可能缺失为 null，Map.of 会 NPE
            Map<String, Object> locationResult = new LinkedHashMap<>();
            locationResult.put("city", city);
            locationResult.put("region", StringUtilExt.defaultIfBlank(json.getString("regionName"), ""));
            locationResult.put("latitude", json.getBigDecimal("lat"));
            locationResult.put("longitude", json.getBigDecimal("lon"));
            locationResult.put("ip", json.getString("query"));
            String resultText = JSON.toJSONString(locationResult);
            LogUtilExt.info(log, "[LocationTool] 当前位置: city={0} lat={1} lon={2}",
                    city, json.getBigDecimal("lat"), json.getBigDecimal("lon"));
            if (Objects.nonNull(trace)) {
                trace.stepWithDuration(TracePhase.TOOL, System.currentTimeMillis() - startMillis,
                        "定位成功：城市={0}，纬度={1}，经度={2}，结果回传模型",
                        city, json.getBigDecimal("lat"), json.getBigDecimal("lon"));
            }
            return Mono.just(ToolResultBlock.text(resultText));
        } catch (RuntimeException e) {
            LogUtilExt.warn(log, "[LocationTool] 定位异常: {0}", e.getMessage());
            if (Objects.nonNull(trace)) {
                trace.stepWithDuration(TracePhase.TOOL, System.currentTimeMillis() - startMillis,
                        "定位异常（无兜底源）：{0}", e.getMessage());
            }
            return Mono.just(ToolResultBlock.error("定位异常: " + e.getMessage()));
        }
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
