package com.dayu.yiyibushe.web.dto;

import com.dayu.yiyibushe.infra.ai.trace.TraceStep;

import java.util.List;

/**
 * 对话接口响应：最终回答 + 命中技能 + 命中工具 + 全链路步骤
 *
 * @param answer
 *     最终回答（天气/新闻结果，或超出范畴固定话术）
 * @param hitSkills
 *     本次命中的技能名（按命中先后；未命中为空列表）
 * @param hitTools
 *     本次命中的工具名（按命中先后；未命中为空列表）
 * @param steps
 *     全链路执行步骤（页面时间线直接渲染）
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record ChatResponse(String answer, List<String> hitSkills, List<String> hitTools,
                           List<TraceStep> steps) {
}
