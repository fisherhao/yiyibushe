package com.dayu.yiyibushe.infra.ai.trace;

/**
 * 全链路单个步骤：由技术代码直接记录，不经过大模型。
 *
 * @param index
 *     步骤序号（从 1 开始，按记录顺序自增）
 * @param timestamp
 *     记录时刻（epoch 毫秒）
 * @param durationMillis
 *     本步骤耗时（毫秒）；仅描述瞬时事件时为 null
 * @param phase
 *     阶段类型（REQUEST / SKILL / TOOL / LLM / AGENT / RESPONSE）
 * @param content
 *     步骤说明（调了哪个方法、关键参数与结果）
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record TraceStep(int index, long timestamp, Long durationMillis, String phase, String content) {
}
