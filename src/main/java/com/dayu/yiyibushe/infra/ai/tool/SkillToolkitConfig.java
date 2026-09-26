package com.dayu.yiyibushe.infra.ai.tool;

import com.dayu.yiyibushe.domain.ai.skill.AiSkill;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.Toolkit;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * AgentScope 工具箱装配：统一注册两类可被 function calling 调用的工具。
 * <p>
 * 1. 领域技能 {@link AiSkill}：经 {@link AiSkillToolAdapter} 适配后注册（如 outfit-advice）；
 * 2. 容器内全部 {@link AgentTool} Bean：直接注册（如 get-current-location、get-weather，
 * 由 SKILL.md 标准 Skill 编排，非 MCP）。
 * <p>
 * 需要工具的 Agent 通过 {@code ReActAgent.Builder#toolkit(Toolkit)} 注入本 Bean。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Configuration
public class SkillToolkitConfig {

    private static final Logger log = LogUtilExt.getLogger(SkillToolkitConfig.class);

    @Autowired
    private List<AiSkill> skills;

    @Autowired
    private List<AgentTool> agentTools;

    /**
     * 构建 AgentScope 工具箱：领域技能经适配器注册，AgentTool Bean 直接注册
     *
     * @return AgentScope 工具箱
     */
    @Bean
    public Toolkit skillToolkit() {
        Toolkit toolkit = new Toolkit();
        for (AiSkill skill : skills) {
            toolkit.registerAgentTool(new AiSkillToolAdapter(skill));
        }
        for (AgentTool agentTool : agentTools) {
            toolkit.registerAgentTool(agentTool);
        }
        int totalTools = CollectionUtilExt.getSize(skills) + CollectionUtilExt.getSize(agentTools);
        LogUtilExt.info(log, "[SkillToolkit] 已注册工具 {0} 个（领域技能 {1} + Skill 工具 {2}）",
                totalTools, CollectionUtilExt.getSize(skills), CollectionUtilExt.getSize(agentTools));
        return toolkit;
    }
}
