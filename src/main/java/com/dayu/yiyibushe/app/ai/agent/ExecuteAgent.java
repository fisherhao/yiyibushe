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
 * 执行 Agent：把规划方案细化为具体的执行说明（为后续图片生成与合成提供指导）。
 * <p>
 * 系统提示与用户消息模板均从提示词库读取，改库即生效。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Component
public class ExecuteAgent extends BaseAiAgent {

    @Autowired
    private PromptStore promptStore;

    {
        name = "execute-agent";
        modelCode = "qwen-flash";
    }

    /**
     * 角色系统提示
     *
     * @return 执行师提示词
     */
    @Override
    protected String buildSystemPrompt() {
        return promptStore.require(AiConstants.PROMPT_EXECUTE_SYSTEM);
    }

    /**
     * 读取规划方案并调用模型细化为执行说明，写回上下文的 executionGuide
     *
     * @param context
     *     执行上下文
     * @param inputs
     *     上游节点结果
     * @return 执行说明结果
     */
    @Override
    public ExecutionResult execute(ExecutionContext context, Map<String, ExecutionResult> inputs) {
        String plan = Optional.ofNullable((String) context.get("plan")).orElse("");
        String executionGuide = callModel(
                promptStore.format(AiConstants.PROMPT_EXECUTE_USER_TEMPLATE, plan));
        context.put("executionGuide", executionGuide);
        return ExecutionResult.success(executionGuide);
    }
}
