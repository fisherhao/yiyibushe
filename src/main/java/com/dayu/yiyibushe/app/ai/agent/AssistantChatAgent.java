package com.dayu.yiyibushe.app.ai.agent;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionContext;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionResult;
import com.dayu.yiyibushe.infra.ai.agent.BaseAiAgent;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocumentLoader;
import com.dayu.yiyibushe.infra.ai.trace.ExecutionTrace;
import com.dayu.yiyibushe.infra.ai.trace.TracePhase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 统一对话 Agent：全部 SKILL.md 技能 + function calling（非 MCP）的运行载体。
 * <p>
 * 无构造器、无 super 链：{@link SkillDocumentLoader} 经字段注入。
 * 系统提示在 {@link #buildSystemPrompt()} 中显式构建，首次对话时懒解析一次：
 * 只含技能 metadata 目录（name + description）用于路由，技能正文不预载——
 * 模型命中后先调 {@code load-skill-instructions} 按需加载，再执行业务工具。
 * 新增技能只需放一个 SKILL.md 目录 + 对应工具 Bean，本类零改动。
 * <p>
 * 能力边界（由模型判断 + 代码兜底双保险）：
 * <ul>
 *   <li>问天气（含"现在冷不冷""会不会下雨"）→ 命中 current-weather；</li>
 *   <li>问科技新闻/科技动态 → 命中 daily-tech-news；</li>
 *   <li>一句话同时问天气和新闻 → 两个技能都加载、工具都调用，合并回答；</li>
 *   <li>超出两类范畴 → 零工具调用，代码兜底返回固定话术。</li>
 * </ul>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Component
public class AssistantChatAgent extends BaseAiAgent {

    /** 超出能力范畴时的固定回复 */
    public static final String OUT_OF_SCOPE_REPLY = "你问的问题超出范畴，我无法对你回答。";

    @Autowired
    private SkillDocumentLoader skillDocumentLoader;

    {
        name = "assistant-chat-agent";
        modelCode = "qwen-flash";
    }

    /**
     * 显式构建系统提示：能力边界 + 懒加载执行规则 + 技能 metadata 目录（不含正文）
     *
     * @return 系统提示文本
     */
    @Override
    protected String buildSystemPrompt() {
        return "你是一个中文助手。你的能力严格限定在以下两个范畴，多一步都不要做：\n"
                + "范畴一：天气查询（当前位置或指定城市的天气、气温、湿度、会不会下雨等）。\n"
                + "范畴二：科技新闻速递（今日科技新闻、科技圈动态、科技界最新消息）。\n"
                + "执行规则（严格遵守）：\n"
                + "1. 先对照下方技能目录，判断用户请求命中哪个技能；\n"
                + "2. 命中后必须先调用 load-skill-instructions 工具（入参 skillName 为目录中的技能名）"
                + "加载该技能的完整执行指令，再按指令调用其业务工具；未加载指令前不得直接调用业务工具；\n"
                + "3. 用户一句话同时命中两个技能时，两个技能的指令分别加载、业务工具全部调用，"
                + "再把两部分结果合并到一条回复中，不能只答一半；\n"
                + "4. 请求与天气、科技新闻都无关（例如讲笑话、问时间、闲聊、编程问题等）时，"
                + "不要调用任何工具；\n"
                + "5. 禁止编造工具返回中没有的事实。\n"
                + skillDocumentLoader.buildMetadataPrompt();
    }

    /**
     * 对话入口：用户自然语言进，模型自行路由技能（不记录执行轨迹）
     *
     * @param userMessage
     *     用户消息
     * @return 模型回复
     */
    public String chat(String userMessage) {
        return chat(userMessage, null);
    }

    /**
     * 对话入口：全链路步骤写入 {@code trace}，并做能力范畴兜底。
     *
     * @param userMessage
     *     用户消息
     * @param trace
     *     全链路执行轨迹（null 时不埋点、不做兜底替换）
     * @return 模型回复；非空提问但零工具命中时返回超出范畴固定话术
     */
    public String chat(String userMessage, ExecutionTrace trace) {
        String reply = callModel(userMessage, trace);

        // 代码兜底：模型没调用任何工具 = 两个范畴都没命中（路由判断仍以模型的工具决策为准）
        if (Objects.nonNull(trace) && StringUtilExt.isNotBlank(userMessage)
                && trace.countByPhase(TracePhase.TOOL) == 0L) {
            trace.step(TracePhase.RESPONSE,
                    "代码兜底判定：模型未调用任何技能工具，问题不在天气/科技新闻范畴，回复固定话术");
            return OUT_OF_SCOPE_REPLY;
        }
        if (Objects.nonNull(trace)) {
            trace.step(TracePhase.RESPONSE, "技能链路完成，模型最终回答返回页面");
        }
        return reply;
    }

    /**
     * 流程编排契约：被节点编排调用时从上下文取 userMessage 对话，回复写回上下文
     *
     * @param context
     *     执行上下文（取 userMessage，写回 reply）
     * @param inputs
     *     上游节点输入
     * @return 对话回复结果
     */
    @Override
    public ExecutionResult execute(ExecutionContext context, Map<String, ExecutionResult> inputs) {
        String userMessage = StringUtilExt.defaultIfBlank((String) context.get("userMessage"), "你好");
        String reply = chat(userMessage);
        context.put("reply", reply);
        return ExecutionResult.success(reply);
    }
}
