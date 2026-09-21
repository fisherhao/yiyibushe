package com.dayu.yiyibushe.infra.ai.tool;

import com.dayu.yiyibushe.domain.ai.skill.AiSkill;
import io.agentscope.core.tool.Toolkit;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * AgentScope 工具箱装配：把所有 {@link AiSkill} 领域技能适配为 AgentScope 工具，
 * 注册进统一的 {@link Toolkit}。
 * <p>
 * 自研 ToolRegistry / ToolDefinition 已删除，工具的发现、Schema 生成、调用分发
 * 全部由 AgentScope Toolkit 承担。需要技能的 Agent 通过
 * {@code ReActAgent.Builder#toolkit(Toolkit)} 注入本 Bean 即可。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Configuration
public class SkillToolkitConfig {

    private static final Logger log = LogUtilExt.getLogger(SkillToolkitConfig.class);

    /**
     * 构建 AgentScope 工具箱：收集容器内全部领域技能并注册
     *
     * @param skills
     *     领域技能列表（Spring 自动注入全部实现）
     * @return AgentScope 工具箱
     */
    @Bean
    public Toolkit skillToolkit(List<AiSkill> skills) {
        Toolkit toolkit = new Toolkit();
        for (AiSkill skill : skills) {
            toolkit.registerAgentTool(new AiSkillToolAdapter(skill));
        }
        LogUtilExt.info(log, "[SkillToolkit] 已注册 AgentScope 技能 {0} 个", skills.size());
        return toolkit;
    }
}