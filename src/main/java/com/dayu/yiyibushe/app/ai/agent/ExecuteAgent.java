package com.dayu.yiyibushe.app.ai.agent;

import com.dayu.yiyibushe.infra.ai.agent.BaseAiAgent;
import com.dayu.yiyibushe.infra.ai.connection.ModelConnectionFactory;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionContext;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionResult;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 执行 Agent：把规划方案细化为具体的执行说明（为后续图片生成与合成提供指导）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class ExecuteAgent extends BaseAiAgent {

    /** 执行角色提示词 */
    private static final String SYSTEM_PROMPT = "你是穿搭执行师。请把规划方案转成一份可直接执行的穿衣合成说明，"
            + "明确每一步使用的衣物类型与画面要求，语言简洁。";

    /**
     * 构造器
     *
     * @param modelRegistry
     *     模型注册表
     * @param connectionFactory
     *     连接工厂
     */
    public ExecuteAgent(ModelRegistry modelRegistry, ModelConnectionFactory connectionFactory) {
        super("execute-agent", "deepseek-chat", SYSTEM_PROMPT, modelRegistry, connectionFactory);
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
        String plan = Objects.requireNonNullElse((String) context.get("plan"), "");
        String executionGuide = callModel("规划方案：\n" + plan);
        context.put("executionGuide", executionGuide);
        return ExecutionResult.success(executionGuide);
    }
}
