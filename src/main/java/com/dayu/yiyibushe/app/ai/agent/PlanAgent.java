package com.dayu.yiyibushe.app.ai.agent;

import com.dayu.yiyibushe.domain.ai.execution.ExecutionContext;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionResult;
import com.dayu.yiyibushe.infra.ai.agent.BaseAiAgent;
import com.dayu.yiyibushe.infra.ai.constant.AiConstants;
import com.dayu.yiyibushe.infra.ai.prompt.PromptStore;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 规划 Agent：分析用户需求，输出可执行的穿衣搭配方案与步骤。
 * <p>
 * 系统提示与用户消息模板均从提示词库读取，改库即生效。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Component
public class PlanAgent extends BaseAiAgent {

    @Autowired
    private PromptStore promptStore;

    {
        name = "plan-agent";
        modelCode = "qwen-flash";
    }

    /**
     * 角色系统提示
     *
     * @return 规划师提示词
     */
    @Override
    protected String buildSystemPrompt() {
        return promptStore.require(AiConstants.PROMPT_PLAN_SYSTEM);
    }

    /**
     * 读取用户需求并调用模型生成搭配规划，写回上下文的 plan
     *
     * @param context
     *     执行上下文
     * @param inputs
     *     上游节点结果
     * @return 规划结果
     */
    @Override
    public ExecutionResult execute(ExecutionContext context, Map<String, ExecutionResult> inputs) {
        String requirement = StringUtilExt.defaultIfBlank((String) context.get("requirement"),
                promptStore.require(AiConstants.PROMPT_DEFAULT_REQUIREMENT));
        String plan = callModel(promptStore.format(AiConstants.PROMPT_PLAN_USER_TEMPLATE, requirement));
        context.put("plan", plan);
        return ExecutionResult.success(plan);
    }
}
