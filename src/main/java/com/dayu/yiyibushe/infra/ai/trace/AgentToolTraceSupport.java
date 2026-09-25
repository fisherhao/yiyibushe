package com.dayu.yiyibushe.infra.ai.trace;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.tool.ToolCallParam;

import java.util.Objects;

/**
 * 工具执行时从 ToolCallParam 解析全链路轨迹的支持类
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public final class AgentToolTraceSupport {

    private AgentToolTraceSupport() {
    }

    /**
     * 从工具入参中取回本次请求的执行轨迹
     *
     * @param param
     *     工具调用参数
     * @return 执行轨迹；直接调用/测试/无 trace 场景返回 null
     */
    public static ExecutionTrace resolve(ToolCallParam param) {
        if (Objects.isNull(param)) {
            return null;
        }
        RuntimeContext runtimeContext = param.getRuntimeContext();
        if (Objects.isNull(runtimeContext)) {
            return null;
        }
        return runtimeContext.get(ExecutionTrace.class);
    }
}
