package com.dayu.yiyibushe.infra.ai.trace;

/**
 * 全链路阶段类型常量
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public final class TracePhase {

    /** HTTP 请求进入（Controller 接收） */
    public static final String REQUEST = "REQUEST";

    /** 技能命中（SKILL.md 被匹配加载） */
    public static final String SKILL = "SKILL";

    /** 工具执行（function calling 命中的具体工具） */
    public static final String TOOL = "TOOL";

    /** 大模型调用（请求发出 / 回复返回） */
    public static final String LLM = "LLM";

    /** Agent 装配（模型绑定、系统提示注入、RuntimeContext 构建） */
    public static final String AGENT = "AGENT";

    /** 响应返回（结果包装、回到页面） */
    public static final String RESPONSE = "RESPONSE";

    private TracePhase() {
    }
}
