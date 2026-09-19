package com.dayu.yiyibushe.domain.ai.agent;

import com.dayu.yiyibushe.domain.ai.execution.ExecutionContext;
import com.dayu.yiyibushe.domain.ai.execution.ExecutionResult;

import java.util.Map;

/**
 * AI 智能体接口：流程编排中的最小执行单元。
 * <p>
 * 接收上游输入与共享上下文，返回执行结果。规划、执行、评审等角色均实现本接口。
 * 接口定义在领域层（描述「Agent 是什么」），技术基座 BaseAiAgent 在 infra 层、
 * 具体业务 Agent 在 app 层实现（依赖倒置）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface AiAgent {

    /**
     * 获取 Agent 名称
     *
     * @return Agent 名称
     */
    String getName();

    /**
     * 执行 Agent 逻辑
     *
     * @param context
     *     执行上下文（共享变量与节点结果）
     * @param inputs
     *     上游节点输入（key 为上游节点 ID）
     * @return 执行结果
     */
    ExecutionResult execute(ExecutionContext context, Map<String, ExecutionResult> inputs);
}
