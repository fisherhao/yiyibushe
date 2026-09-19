package com.dayu.yiyibushe.app.ai.agent;

import com.dayu.yiyibushe.infra.ai.connection.ModelConnectionFactory;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionContext;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionResult;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import com.dayu.yiyibushe.infra.ai.agent.BaseAiAgent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 规划 Agent：分析用户需求，输出可执行的穿衣搭配方案与步骤。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class PlanAgent extends BaseAiAgent {

    /** 规划角色提示词 */
    private static final String SYSTEM_PROMPT = "你是专业穿搭规划师。根据用户描述的风格、场景和效果，"
            + "结合用户上传的人物图与衣物，输出一份简洁的搭配方案：包含选用哪些衣物、搭配顺序与预期效果。";

    /**
     * 构造器
     *
     * @param modelRegistry
     *     模型注册表
     * @param connectionFactory
     *     连接工厂
     */
    public PlanAgent(ModelRegistry modelRegistry, ModelConnectionFactory connectionFactory) {
        super("plan-agent", "deepseek-chat", SYSTEM_PROMPT, modelRegistry, connectionFactory);
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
        String requirement = Objects.requireNonNullElse((String) context.get("requirement"), "生成一套日常穿搭");
        String plan = callModel("用户需求：" + requirement);
        context.put("plan", plan);
        return ExecutionResult.success(plan);
    }
}
