package com.dayu.yiyibushe.infra.ai.skill;

/**
 * Skill 文档：对应 Agent Skills 开放标准（agentskills.io）中一个 SKILL.md 的解析结果。
 *
 * @param name
 *     技能名（frontmatter，启动时预加载，需与目录名一致）
 * @param description
 *     技能描述（frontmatter，what + when，模型据此判断是否触发，启动时预加载）
 * @param instructions
 *     SKILL.md 正文（工作流、工具用法、示例），仅在技能被命中时才注入模型上下文
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record SkillDocument(String name, String description, String instructions) {
}
