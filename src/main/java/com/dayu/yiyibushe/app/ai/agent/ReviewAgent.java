package com.dayu.yiyibushe.app.ai.agent;

import com.dayu.yiyibushe.domain.ai.execution.ExecutionContext;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionResult;
import com.dayu.yiyibushe.infra.ai.agent.BaseAiAgent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 评审 Agent：对执行结果做最终把关，输出可交付的最终版本。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Component
public class ReviewAgent extends BaseAiAgent {

    /** 评审角色提示词 */
    private static final String SYSTEM_PROMPT = "你是穿搭评审师。请检查执行说明是否符合用户的风格与场景要求，"
            + "给出最终优化后的可交付版本。直接输出最终内容，不要多余解释。";

    {
        name = "review-agent";
        modelCode = "qwen-flash";
    }

    /**
     * 角色系统提示
     *
     * @return 评审师提示词
     */
    @Override
    protected String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * 读取执行说明并调用模型做最终评审优化，写回上下文的 finalContent
     *
     * @param context
     *     执行上下文
     * @param inputs
     *     上游节点结果
     * @return 评审后的最终结果
     */
    @Override
    public ExecutionResult execute(ExecutionContext context, Map<String, ExecutionResult> inputs) {
        String executionGuide = Objects.requireNonNullElse((String) context.get("executionGuide"), "");
        String finalContent = callModel("执行内容：\n" + executionGuide);
        context.put("finalContent", finalContent);
        return ExecutionResult.success(finalContent);
    }
}
