package com.dayu.yiyibushe.skill.news;

import com.dayu.yiyibushe.app.ai.agent.AssistantChatAgent;
import com.dayu.yiyibushe.app.ai.tool.GetCurrentLocationTool;
import com.dayu.yiyibushe.app.ai.tool.GetTechNewsTool;
import com.dayu.yiyibushe.app.ai.tool.GetWeatherTool;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocument;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocumentLoader;
import com.dayu.yiyibushe.infra.ai.trace.ExecutionTrace;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * daily-tech-news 技能路由测试：{@code @SpringBootTest} 启动完整容器。
 * <p>
 * 验证统一路由：
 * <ul>
 *   <li>问科技新闻 → 命中 get-tech-news，输出 3-5 条中文要点（标题/来源/总结/链接），
 *       天气工具不触发；</li>
 *   <li>问笑话 → 所有工具零调用，回复超出范畴固定话术；</li>
 *   <li>一句话同时问天气和新闻 → 两个技能、对应工具全部命中；</li>
 *   <li>10 分钟内再问一次 → 工具仍被模型命中，但外部请求次数不增加（缓存生效）。</li>
 * </ul>
 *
 * 运行方式（项目根目录）：
 * <pre>
 *   mvn test -Dtest=NewsSkillTest
 * </pre>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@SpringBootTest
class NewsSkillTest {

    private static final Logger log = LogUtilExt.getLogger(NewsSkillTest.class);

    /** SKILL.md 加载器 */
    @Autowired
    private SkillDocumentLoader skillDocumentLoader;

    /** 统一对话 Agent */
    @Autowired
    private AssistantChatAgent assistantChatAgent;

    /** 新闻工具（读取命中与外部请求计数） */
    @Autowired
    private GetTechNewsTool getTechNewsTool;

    /** 定位、天气工具（验证问新闻时不会误命中） */
    @Autowired
    private GetCurrentLocationTool getCurrentLocationTool;

    @Autowired
    private GetWeatherTool getWeatherTool;

    /**
     * 技能文档加载：frontmatter 与正文均解析成功
     */
    @Test
    void shouldLoadNewsSkillDocument() {
        SkillDocument newsSkill = skillDocumentLoader.getSkill("daily-tech-news");
        assertNotNull(newsSkill, "daily-tech-news 的 SKILL.md 未加载");
        assertEquals("daily-tech-news", newsSkill.name());
        assertTrue(newsSkill.description().contains("新闻"));
        assertTrue(newsSkill.instructions().contains("get-tech-news"));
        assertTrue(newsSkill.instructions().contains("GDELT"));
    }

    /**
     * 问科技新闻：命中新闻工具，不触发任何天气工具，回复为结构化中文要点
     */
    @Test
    void shouldHitNewsToolWhenAskingTechNews() {
        int newsBefore = getTechNewsTool.getCallCount();
        int locationBefore = getCurrentLocationTool.getCallCount();
        int weatherBefore = getWeatherTool.getCallCount();

        ExecutionTrace trace = new ExecutionTrace();
        String reply = assistantChatAgent.chat("给我看看今天有什么科技新闻", trace);
        LogUtilExt.info(log, "[NewsSkillTest] 科技新闻回复:\n{0}", reply);

        assertTrue(getTechNewsTool.getCallCount() > newsBefore,
                "问科技新闻应命中 get-tech-news");
        assertEquals(locationBefore, getCurrentLocationTool.getCallCount(),
                "问新闻不应命中定位工具");
        assertEquals(weatherBefore, getWeatherTool.getCallCount(),
                "问新闻不应命中天气工具");

        // 输出格式校验：含编号条目和链接，且不是原始 JSON 回显
        assertTrue(reply.contains("http"), "回复缺少新闻链接: " + reply);
        assertTrue(reply.contains("1."), "回复未按编号列表输出: " + reply);
        assertTrue(!reply.startsWith("[{"), "回复不应是原始 JSON: " + reply);
        assertEquals(List.of("daily-tech-news"), trace.getHitSkills());
        // 顺序断言（容忍模型幂重地重复加载）：懒加载必须发生在业务工具之前
        List<String> hitTools = trace.getHitTools();
        assertEquals("load-skill-instructions", hitTools.get(0),
                "第一步应先懒加载技能正文");
        assertTrue(hitTools.contains("get-tech-news"), "随后应执行 get-tech-news");
    }

    /**
     * 问笑话：超出两个范畴，所有工具零调用，回复固定话术
     */
    @Test
    void shouldReplyOutOfScopeWhenAskingJoke() {
        int newsBefore = getTechNewsTool.getCallCount();
        int locationBefore = getCurrentLocationTool.getCallCount();
        int weatherBefore = getWeatherTool.getCallCount();

        ExecutionTrace trace = new ExecutionTrace();
        String reply = assistantChatAgent.chat("给我讲个笑话吧", trace);
        LogUtilExt.info(log, "[NewsSkillTest] 问笑话回复: {0}", reply);

        assertEquals(AssistantChatAgent.OUT_OF_SCOPE_REPLY, reply,
                "超出范畴的问题应回复固定话术");
        assertEquals(newsBefore, getTechNewsTool.getCallCount(), "问笑话不应命中新闻工具");
        assertEquals(locationBefore, getCurrentLocationTool.getCallCount(), "问笑话不应命中定位工具");
        assertEquals(weatherBefore, getWeatherTool.getCallCount(), "问笑话不应命中天气工具");
        assertTrue(trace.getHitSkills().isEmpty(), "问笑话不应命中任何技能");
    }

    /**
     * 一句话同时问天气和新闻：两个技能都命中，指定城市天气工具 + 新闻工具都调用
     */
    @Test
    void shouldHitBothSkillsWhenAskingWeatherAndNewsTogether() {
        int newsBefore = getTechNewsTool.getCallCount();
        int weatherBefore = getWeatherTool.getCallCount();
        int locationBefore = getCurrentLocationTool.getCallCount();

        ExecutionTrace trace = new ExecutionTrace();
        String reply = assistantChatAgent.chat(
                "帮我看看北京今天天气怎么样，顺便给我看看今天有什么科技新闻", trace);
        LogUtilExt.info(log, "[NewsSkillTest] 双意图回复:\n{0}", reply);

        assertTrue(getWeatherTool.getCallCount() > weatherBefore, "双意图应命中 get-weather");
        assertTrue(getTechNewsTool.getCallCount() > newsBefore, "双意图应命中 get-tech-news");
        assertEquals(locationBefore, getCurrentLocationTool.getCallCount(),
                "已指定城市，不应命中定位工具");
        assertTrue(reply.contains("℃"), "回复未包含天气结果: " + reply);
        assertTrue(reply.contains("http"), "回复未包含新闻结果: " + reply);

        List<String> hitSkills = trace.getHitSkills();
        assertTrue(hitSkills.contains("current-weather"), "应命中天气技能");
        assertTrue(hitSkills.contains("daily-tech-news"), "应命中新闻技能");
        List<String> hitTools = trace.getHitTools();
        assertTrue(hitTools.indexOf("load-skill-instructions") < hitTools.indexOf("get-weather"),
                "应先懒加载技能正文，再调用业务工具");
        assertTrue(hitTools.contains("get-weather"), "应调用天气工具");
        assertTrue(hitTools.contains("get-tech-news"), "应调用新闻工具");
    }

    /**
     * 缓存验证：连续两次问新闻，第二次外部请求次数不增加（10 分钟缓存）
     */
    @Test
    void shouldUseCacheWhenAskingNewsRepeatedly() {
        // 第一次：确保有数据（可能首次加载）
        assistantChatAgent.chat("今天科技圈有什么大事？");
        int externalRequestsAfterFirst = getTechNewsTool.getExternalRequestCount();

        // 第二次：模型仍会调用工具，但工具内部直接返回缓存，不打外网
        assistantChatAgent.chat("再给我看一遍今天的科技新闻");
        int externalRequestsAfterSecond = getTechNewsTool.getExternalRequestCount();

        LogUtilExt.info(log, "[NewsSkillTest] 外部请求次数 第一次后={0} 第二次后={1}",
                externalRequestsAfterFirst, externalRequestsAfterSecond);
        assertEquals(externalRequestsAfterFirst, externalRequestsAfterSecond,
                "10 分钟内重复询问应命中缓存，外部请求次数不应增加");
    }
}
