package com.dayu.yiyibushe.app.ai.agent;

import com.dayu.yiyibushe.domain.ai.execution.ExecutionContext;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionResult;
import com.dayu.yiyibushe.infra.ai.agent.BaseAiAgent;
import com.dayu.yiyibushe.infra.ai.constant.AiConstants;
import com.dayu.yiyibushe.infra.ai.prompt.PromptStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 评审 Agent：对执行结果做最终把关，输出可交付的最终版本。
 * <p>
 * 系统提示与用户消息模板均从提示词库读取，改库即生效。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Component
public class ReviewAgent extends BaseAiAgent {

    @Autowired
    private PromptStore promptStore;

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
        return promptStore.require(AiConstants.PROMPT_REVIEW_SYSTEM);
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
        String executionGuide = Optional.ofNullable((String) context.get("executionGuide")).orElse("");
        String finalContent = callModel(
                promptStore.format(AiConstants.PROMPT_REVIEW_USER_TEMPLATE, executionGuide));
        context.put("finalContent", finalContent);
        return ExecutionResult.success(finalContent);
    }
}
