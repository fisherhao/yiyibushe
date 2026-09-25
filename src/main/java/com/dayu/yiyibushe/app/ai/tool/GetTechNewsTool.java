package com.dayu.yiyibushe.app.ai.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 科技新闻抓取工具（function calling，非 MCP）：daily-tech-news 技能的取数工具。
 * <p>
 * 三级免费信源（均无需 API Key），任一成功即返回：
 * <ol>
 *   <li>GDELT DOC 2.0（主源，失败重试 1 次，识别其限流提示）；</li>
 *   <li>Hacker News 官方 Firebase API（稳定兜底，取热榜前若干条）；</li>
 *   <li>Wikinews RSS（JDK 原生 DOM 解析，不引第三方库）。</li>
 * </ol>
 * 简单内存缓存：10 分钟内重复命中直接返回，避免高频打外部源（也正好规避 GDELT 限频）。
 * 调用计数 {@link #getCallCount()} 与外部请求计数 {@link #getExternalRequestCount()}
 * 分别供测试验证"是否被模型命中"和"缓存是否生效"。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class GetTechNewsTool implements AgentTool {

    private static final Logger log = LogUtilExt.getLogger(GetTechNewsTool.class);

    /** 工具名（模型 function calling 选择用，需唯一） */
    private static final String TOOL_NAME = "get-tech-news";

    /** 所属技能名（对应 daily-tech-news/SKILL.md） */
    private static final String SKILL_NAME = "daily-tech-news";

    /** GDELT 主源：URL 直接预编码写死（%20 已编码），不经模板变量展开以免二次编码 */
    private static final String GDELT_API = "https://api.gdeltproject.org/api/v2/doc/doc"
            + "?query=(technology%20OR%20%22artificial%20intelligence%22%20OR%20software%20OR%20startup)"
            + "&mode=artlist&maxrecords=15&format=json&sort=hybridrel&sourcelang=english";

    /** Wikinews RSS 兜底：已发布新闻分类 feed（RecentChanges 含大量非新闻编辑活动，不可用） */
    private static final String WIKINEWS_RSS =
            "https://en.wikinews.org/w/index.php?title=Category:Published&feed=rss";

    /** Hacker News 热榜兜底 */
    private static final String HN_TOPSTORIES_API =
            "https://hacker-news.firebaseio.com/v0/topstories.json";

    private static final String HN_ITEM_API =
            "https://hacker-news.firebaseio.com/v0/item/{id}.json";

    /** 缓存有效期 10 分钟（新闻更新频率低） */
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /** HN 拉取条数上限（逐个请求，控制耗时） */
    private static final int HN_FETCH_LIMIT = 10;

    /** 命中次数：模型调用本工具的次数 */
    private final AtomicInteger callCount = new AtomicInteger();

    /** 外部请求次数：用于验证缓存命中时不再打外网 */
    private final AtomicInteger externalRequestCount = new AtomicInteger();

    /** 共享 HTTP 客户端（RestClientConfig 统一注入） */
    @Autowired
    private RestClient restClient;

    /** 缓存条目（null 表示无缓存） */
    private volatile CachedNews cachedNews;

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
        return "抓取今日最新科技新闻条目（标题、链接、来源、发布时间），无参数。"
                + "当用户询问科技新闻、科技圈动态、最近有什么新消息时调用；"
                + "返回原始新闻列表后，由你把外文标题翻译成中文并提炼 3-5 条要点，不要原样输出 JSON。";
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
     * 执行抓取：缓存优先 → GDELT（重试 1 次）→ Hacker News → Wikinews
     *
     * @param param
     *     调用参数（本工具无入参，不使用）
     * @return 新闻 JSON 数组文本或错误块
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
        }

        // 1. 缓存命中直接返回，不打外网
        CachedNews snapshot = cachedNews;
        if (snapshot != null && !snapshot.isExpired()) {
            long remainSeconds = Duration.between(Instant.now(), snapshot.expireAt()).toSeconds();
            LogUtilExt.info(log, "[NewsTool] 命中缓存（剩余 {0} 秒），外部请求数不变", remainSeconds);
            if (Objects.nonNull(trace)) {
                trace.stepWithDuration(TracePhase.TOOL,
                        System.currentTimeMillis() - startMillis,
                        "命中 10 分钟内存缓存（剩余 {0} 秒），零外部请求，缓存结果回传模型",
                        remainSeconds);
            }
            return Mono.just(ToolResultBlock.text(snapshot.newsJson()));
        }
        if (Objects.nonNull(trace)) {
            trace.step(TracePhase.TOOL, "缓存无有效条目，按 主源→兜底 顺序请求外部信源");
        }

        // 2. GDELT 主源：失败重试 1 次（间隔 2 秒，兼顾客服 5 秒限频要求）
        String newsJson = fetchFromGdelt(1, trace);
        if (StringUtilExt.isBlank(newsJson)) {
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "GDELT 第 1 次请求未成功，等待 2 秒后重试 1 次");
            }
            sleepQuietly(Duration.ofSeconds(2));
            newsJson = fetchFromGdelt(2, trace);
        }
        // 3. Hacker News 兜底：稳定可靠的科技热榜
        if (StringUtilExt.isBlank(newsJson)) {
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "GDELT 不可用，切换兜底信源 Hacker News");
            }
            newsJson = fetchFromHackerNews(trace);
        }
        // 4. Wikinews 兜底：已发布新闻分类 feed
        if (StringUtilExt.isBlank(newsJson)) {
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "Hacker News 不可用，切换兜底信源 Wikinews RSS");
            }
            newsJson = fetchFromWikinews(trace);
        }

        if (StringUtilExt.isNotBlank(newsJson)) {
            cachedNews = new CachedNews(newsJson, Instant.now().plus(CACHE_TTL));
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "抓取成功，结果写入 10 分钟内存缓存，原始列表回传模型");
                trace.stepWithDuration(TracePhase.TOOL,
                        System.currentTimeMillis() - startMillis, "工具执行结束");
            }
            return Mono.just(ToolResultBlock.text(newsJson));
        }
        if (Objects.nonNull(trace)) {
            trace.stepWithDuration(TracePhase.TOOL,
                    System.currentTimeMillis() - startMillis,
                    "三个免费新闻源（GDELT、Hacker News、Wikinews）均不可用");
        }
        return Mono.just(ToolResultBlock.error(
                "三个免费新闻源（GDELT、Wikinews、Hacker News）均不可用"));
    }

    /**
     * GDELT 主源：解析 artlist JSON；识别限频文本与结构异常
     *
     * @param attempt
     *     第几次尝试（trace 展示用）
     * @param trace
     *     执行轨迹（可空）
     * @return 新闻 JSON 数组文本，失败返回 null
     */
    private String fetchFromGdelt(int attempt, ExecutionTrace trace) {
        try {
            externalRequestCount.incrementAndGet();
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "第 {0} 次请求主源 GDELT DOC 2.0（8秒超时）", attempt);
            }
            String body = restClient.get().uri(GDELT_API).retrieve().body(String.class);
            // GDELT 限频时返回 200 + 纯文本提示
            if (StringUtilExt.isBlank(body) || body.startsWith("Please limit requests")
                    || !body.trim().startsWith("{")) {
                LogUtilExt.warn(log, "[NewsTool] GDELT 不可用（限频或非 JSON）: {0}",
                        StringUtilExt.substring(StringUtilExt.isBlank(body) ? "" : body.trim(), 0, 120));
                if (Objects.nonNull(trace)) {
                    trace.step(TracePhase.TOOL,
                            "GDELT 返回限频提示或非 JSON 内容（HTTP 200），本次尝试放弃");
                }
                return null;
            }
            JSONArray articles = JSON.parseObject(body).getJSONArray("articles");
            if (articles == null || articles.isEmpty()) {
                return null;
            }
            List<Map<String, Object>> newsItems = new ArrayList<>();
            for (int i = 0; i < articles.size(); i++) {
                JSONObject article = articles.getJSONObject(i);
                newsItems.add(Map.of(
                        "title", article.getString("title"),
                        "url", article.getString("url"),
                        "source", StringUtilExt.defaultIfBlank(article.getString("domain"), "GDELT"),
                        "publishedAt", StringUtilExt.defaultString(article.getString("seendate"))));
            }
            LogUtilExt.info(log, "[NewsTool] GDELT 取到 {0} 条", newsItems.size());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "GDELT 解析成功，取到 {0} 条科技新闻", newsItems.size());
            }
            return JSON.toJSONString(newsItems);
        } catch (RuntimeException e) {
            LogUtilExt.warn(log, "[NewsTool] GDELT 请求异常: {0}", e.getMessage());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "GDELT 请求异常：{0}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Wikinews RSS 兜底：JDK DOM 解析 item 节点
     *
     * @param trace
     *     执行轨迹（可空）
     * @return 新闻 JSON 数组文本，失败返回 null
     */
    private String fetchFromWikinews(ExecutionTrace trace) {
        try {
            externalRequestCount.incrementAndGet();
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "请求 Wikinews 已发布分类 RSS（8秒超时）");
            }
            String xml = restClient.get().uri(WIKINEWS_RSS).retrieve().body(String.class);
            if (StringUtilExt.isBlank(xml)) {
                return null;
            }
            // 禁用 DTD/外部实体：防止 XXE，也避免解析器联网卡顿
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList itemNodes = document.getElementsByTagName("item");
            List<Map<String, Object>> newsItems = new ArrayList<>();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element item = (Element) itemNodes.item(i);
                newsItems.add(Map.of(
                        "title", item.getElementsByTagName("title").item(0).getTextContent(),
                        "url", item.getElementsByTagName("link").item(0).getTextContent(),
                        "source", "Wikinews",
                        "publishedAt", item.getElementsByTagName("pubDate").item(0).getTextContent()));
            }
            if (newsItems.isEmpty()) {
                return null;
            }
            LogUtilExt.info(log, "[NewsTool] Wikinews 取到 {0} 条", newsItems.size());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "Wikinews RSS 解析成功，取到 {0} 条", newsItems.size());
            }
            return JSON.toJSONString(newsItems);
        } catch (Exception e) {
            LogUtilExt.warn(log, "[NewsTool] Wikinews 解析异常: {0}", e.getMessage());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "Wikinews RSS 解析异常：{0}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Hacker News 兜底：热榜前 N 条逐个取详情
     *
     * @param trace
     *     执行轨迹（可空）
     * @return 新闻 JSON 数组文本，失败返回 null
     */
    private String fetchFromHackerNews(ExecutionTrace trace) {
        try {
            externalRequestCount.incrementAndGet();
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "请求 Hacker News 热榜 topstories（随后逐条取详情）");
            }
            String idListJson = restClient.get().uri(HN_TOPSTORIES_API).retrieve().body(String.class);
            JSONArray idArray = JSON.parseArray(idListJson);

            List<Map<String, Object>> newsItems = new ArrayList<>();
            int fetchCount = Math.min(HN_FETCH_LIMIT, idArray.size());
            for (int i = 0; i < fetchCount; i++) {
                long storyId = idArray.getLongValue(i);
                try {
                    String itemJson = restClient.get()
                            .uri(HN_ITEM_API, storyId)
                            .retrieve()
                            .body(String.class);
                    JSONObject story = JSON.parseObject(itemJson);
                    if (!"story".equals(story.getString("type"))) {
                        continue;
                    }
                    String url = StringUtilExt.defaultIfBlank(story.getString("url"),
                            "https://news.ycombinator.com/item?id=" + storyId);
                    newsItems.add(Map.of(
                            "title", story.getString("title"),
                            "url", url,
                            "source", "Hacker News ("
                                    + StringUtilExt.defaultString(story.getString("by")) + ")",
                            "publishedAt", Instant.ofEpochSecond(
                                    story.getLongValue("time")).toString()));
                } catch (RuntimeException itemException) {
                    // 单条失败跳过，不影响其他条目
                    LogUtilExt.warn(log, "[NewsTool] HN 单条 {0} 失败: {1}",
                            storyId, itemException.getMessage());
                }
            }
            if (newsItems.isEmpty()) {
                return null;
            }
            LogUtilExt.info(log, "[NewsTool] Hacker News 取到 {0} 条", newsItems.size());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "Hacker News 详情解析成功，取到 {0} 条", newsItems.size());
            }
            return JSON.toJSONString(newsItems);
        } catch (RuntimeException e) {
            LogUtilExt.warn(log, "[NewsTool] Hacker News 请求异常: {0}", e.getMessage());
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "Hacker News 请求异常：{0}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * 安静睡眠（中断时恢复中断标记）
     *
     * @param duration
     *     睡眠时长
     */
    private void sleepQuietly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取工具被模型命中的次数
     *
     * @return 命中次数
     */
    public int getCallCount() {
        return callCount.get();
    }

    /**
     * 获取外部源实际请求次数（验证缓存是否生效）
     *
     * @return 外部请求次数
     */
    public int getExternalRequestCount() {
        return externalRequestCount.get();
    }

    /**
     * 新闻缓存条目：内容 + 过期时刻
     */
    private record CachedNews(String newsJson, Instant expireAt) {

        /**
         * 是否仍在有效期
         *
         * @return true 表示未过期
         */
        boolean isExpired() {
            return Instant.now().isAfter(expireAt);
        }
    }
}
