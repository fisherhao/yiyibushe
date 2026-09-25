package com.dayu.yiyibushe.skill.weather;

import com.dayu.yiyibushe.app.ai.agent.AssistantChatAgent;
import com.dayu.yiyibushe.app.ai.tool.GetCurrentLocationTool;
import com.dayu.yiyibushe.app.ai.tool.GetTechNewsTool;
import com.dayu.yiyibushe.app.ai.tool.GetWeatherTool;
import com.dayu.yiyibushe.app.ai.tool.LoadSkillInstructionsTool;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocument;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocumentLoader;
import com.dayu.yiyibushe.infra.ai.trace.ExecutionTrace;
import com.dayu.yiyibushe.infra.ai.trace.TracePhase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * current-weather 技能路由测试：{@code @SpringBootTest} 启动完整容器。
 * <p>
 * 所有调用都从用户自然语言出发，经统一 {@link AssistantChatAgent} 路由，
 * 由 qwen-flash 自行判断命中哪个技能。工具计数与执行轨迹作为客观证据：
 * <ul>
 * <li>问时间/日期（带 trace）→ 工具零命中，代码兜底回复超出范畴固定话术；</li>
 * <li>问当前位置天气 → get-current-location、get-weather 依次命中，新闻工具不触发；</li>
 * <li>问指定城市天气 → 只命中 get-weather；</li>
 * <li>trace 中 REQUEST/AGENT/LLM/TOOL/RESPONSE 各阶段齐全。</li>
 * </ul>
 *
 * 运行方式（项目根目录）：
 * 
 * <pre>
 *   mvn test -Dtest=WeatherSkillTest
 * </pre>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.5
 */
@SpringBootTest
class WeatherSkillTest {

    private static final Logger log = LogUtilExt.getLogger(WeatherSkillTest.class);

    /** SKILL.md 加载器 */
    @Autowired
    private SkillDocumentLoader skillDocumentLoader;

    /** 统一对话 Agent */
    @Autowired
    private AssistantChatAgent assistantChatAgent;

    /** 定位工具（读取命中计数） */
    @Autowired
    private GetCurrentLocationTool getCurrentLocationTool;

    /** 天气工具（读取命中计数） */
    @Autowired
    private GetWeatherTool getWeatherTool;

    /** 新闻工具（验证问天气时不会误命中） */
    @Autowired
    private GetTechNewsTool getTechNewsTool;

    /** 懒加载工具（验证命中技能后先加载正文） */
    @Autowired
    private LoadSkillInstructionsTool loadSkillInstructionsTool;

    /**
     * 元数据加载：两个技能的 frontmatter 与正文均需解析成功
     */
    @Test
    void shouldLoadSkillDocument() {
        SkillDocument weather = skillDocumentLoader.getSkill("current-weather");
        assertNotNull(weather, "current-weather 的 SKILL.md 未加载");
        assertEquals("current-weather", weather.name());
        assertTrue(weather.description().contains("天气"));
        assertTrue(weather.instructions().contains("get-current-location"));
        assertNotNull(skillDocumentLoader.getSkill("daily-tech-news"),
                "daily-tech-news 的 SKILL.md 未加载");
        LogUtilExt.info(log, "[WeatherSkillTest] 已加载技能数={0}",
                skillDocumentLoader.listSkills().size());
    }

    /**
     * 问时间：超出两个范畴，所有工具零命中，回复固定话术
     */
    @Test
    void shouldReplyOutOfScopeWhenAskingTime() {
        int locationBefore = getCurrentLocationTool.getCallCount();
        int weatherBefore = getWeatherTool.getCallCount();
        int newsBefore = getTechNewsTool.getCallCount();
        int loadBefore = loadSkillInstructionsTool.getCallCount();

        ExecutionTrace trace = new ExecutionTrace();
        String reply = assistantChatAgent.chat("现在几点了？今天星期几？", trace);
        LogUtilExt.info(log, "[WeatherSkillTest] 问时间回复: {0}", reply);

        assertEquals(AssistantChatAgent.OUT_OF_SCOPE_REPLY, reply,
                "超出范畴的问题应回复固定话术");
        assertEquals(locationBefore, getCurrentLocationTool.getCallCount(), "问时间不应命中定位工具");
        assertEquals(weatherBefore, getWeatherTool.getCallCount(), "问时间不应命中天气工具");
        assertEquals(newsBefore, getTechNewsTool.getCallCount(), "问时间不应命中新闻工具");
        assertEquals(loadBefore, loadSkillInstructionsTool.getCallCount(), "问时间不应触发懒加载");
        assertTrue(trace.getHitSkills().isEmpty(), "问时间不应命中任何技能");
    }

    /**
     * 问当前位置天气：依次命中定位、天气工具，且不触发新闻工具；trace 全链路可验证
     */
    @Test
    void shouldHitBothToolsWhenAskingCurrentWeather() {
        int locationBefore = getCurrentLocationTool.getCallCount();
        int weatherBefore = getWeatherTool.getCallCount();
        int newsBefore = getTechNewsTool.getCallCount();
        int loadBefore = loadSkillInstructionsTool.getCallCount();

        ExecutionTrace trace = new ExecutionTrace();
        trace.step(TracePhase.REQUEST, "测试直调 Agent，模拟 HTTP 请求进入");
        String reply = assistantChatAgent.chat("我这边现在天气怎么样呀？", trace);
        LogUtilExt.info(log, "[WeatherSkillTest] 当前位置天气回复: {0}", reply);

        assertTrue(loadSkillInstructionsTool.getCallCount() > loadBefore,
                "命中技能后应先调 load-skill-instructions 懒加载正文");
        assertTrue(getCurrentLocationTool.getCallCount() > locationBefore,
                "懒加载后应命中 get-current-location");
        assertTrue(getWeatherTool.getCallCount() > weatherBefore,
                "随后应命中 get-weather");
        assertEquals(newsBefore, getTechNewsTool.getCallCount(), "问天气不应命中新闻工具");
        assertTrue(reply.contains("℃"), "回复未包含温度: " + reply);

        // 技能与工具维度：懒加载必须在最前，业务工具按"先定位再查天气"（容忍模型重复加载）
        assertEquals(List.of("current-weather"), trace.getHitSkills());
        List<String> hitTools = trace.getHitTools();
        assertEquals("load-skill-instructions", hitTools.get(0), "第一步先懒加载技能正文");
        assertTrue(hitTools.indexOf("get-current-location") < hitTools.indexOf("get-weather"),
                "第二步先定位，第三步再查天气");
        assertPhasesComplete(trace);
    }

    /**
     * 问指定城市天气：只命中天气工具，不需要定位、不抓新闻
     */
    @Test
    void shouldOnlyHitWeatherToolWhenAskingCityWeather() {
        int locationBefore = getCurrentLocationTool.getCallCount();
        int weatherBefore = getWeatherTool.getCallCount();
        int newsBefore = getTechNewsTool.getCallCount();

        ExecutionTrace trace = new ExecutionTrace();
        trace.step(TracePhase.REQUEST, "测试直调 Agent，模拟 HTTP 请求进入");
        String reply = assistantChatAgent.chat("帮我看看上海今天什么天气？", trace);
        LogUtilExt.info(log, "[WeatherSkillTest] 指定城市天气回复: {0}", reply);

        assertEquals(locationBefore, getCurrentLocationTool.getCallCount(),
                "已指定城市，不应命中定位工具");
        assertTrue(getWeatherTool.getCallCount() > weatherBefore,
                "应命中 get-weather");
        assertEquals(newsBefore, getTechNewsTool.getCallCount(), "问城市天气不应命中新闻工具");
        assertTrue(reply.contains("℃"), "回复未包含温度: " + reply);
        List<String> hitTools = trace.getHitTools();
        assertEquals("load-skill-instructions", hitTools.get(0), "应先懒加载技能正文");
        assertTrue(hitTools.contains("get-weather"), "应执行 get-weather");
        assertTrue(!hitTools.contains("get-current-location"), "已指定城市，不应定位");
        assertPhasesComplete(trace);
    }

    /**
     * 断言全链路关键阶段齐全：REQUEST → AGENT/LLM → TOOL → RESPONSE
     *
     * @param trace
     *              执行轨迹
     */
    private void assertPhasesComplete(ExecutionTrace trace) {
        assertTrue(trace.countByPhase(TracePhase.REQUEST) > 0, "缺少 REQUEST 阶段");
        assertTrue(trace.countByPhase(TracePhase.AGENT) > 0, "缺少 AGENT 阶段");
        assertTrue(trace.countByPhase(TracePhase.LLM) > 0, "缺少 LLM 阶段");
        assertTrue(trace.countByPhase(TracePhase.TOOL) > 0, "缺少 TOOL 阶段");
        assertTrue(trace.countByPhase(TracePhase.RESPONSE) > 0, "缺少 RESPONSE 阶段");
    }
}
