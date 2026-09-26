package com.dayu.yiyibushe.infra.ai.tool;

import com.dayu.yiyibushe.domain.ai.skill.AiSkill;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 领域技能 → AgentScope 工具适配器：把 {@link AiSkill} 包装成 AgentScope
 * {@link AgentTool}，交给 {@code Toolkit} 注册，供 ReActAgent 的 function calling 调用。
 * <p>
 * 入参约定：模型按 JSON Schema 传 {@code parameters}（自由键值对象），
 * 适配器解包后交给技能执行；若模型直接给平铺键值对则原样透传。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class AiSkillToolAdapter implements AgentTool {

    /** 入参包装键名 */
    private static final String PARAM_NAME = "parameters";

    /** 被适配的领域技能 */
    private AiSkill skill;

    /**
     * 静态工厂：把领域技能适配为 AgentScope 工具
     *
     * @param skill
     *     领域技能
     * @return 适配器
     */
    public static AiSkillToolAdapter create(AiSkill skill) {
        AiSkillToolAdapter adapter = new AiSkillToolAdapter();
        adapter.skill = skill;
        return adapter;
    }

    /**
     * 获取工具名称（即技能名称，供模型 function calling 选择）
     *
     * @return 工具名称
     */
    @Override
    public String getName() {
        return skill.getName();
    }

    /**
     * 获取工具描述（即技能描述，供模型判断调用时机）
     *
     * @return 工具描述
     */
    @Override
    public String getDescription() {
        return skill.getDescription();
    }

    /**
     * 获取入参 JSON Schema：单个自由键值对象 parameters，具体键由技能描述约束
     *
     * @return JSON Schema
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameterSchema = Map.of(
                "type", "object",
                "description", "技能入参键值对，键名与取值由技能描述约束");
        return Map.of(
                "type", "object",
                "properties", Map.of(PARAM_NAME, parameterSchema),
                "required", List.of(PARAM_NAME));
    }

    /**
     * 执行工具：解包入参并委托技能，异常统一转为错误结果块
     *
     * @param param
     *     工具调用参数（含模型给出的入参）
     * @return 技能执行结果
     */
    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        try {
            return Mono.just(ToolResultBlock.text(skill.invoke(unwrapInput(param.getInput()))));
        } catch (RuntimeException e) {
            return Mono.just(ToolResultBlock.error(e.getMessage()));
        }
    }

    /**
     * 解包工具入参：若模型按 Schema 包了一层 parameters 则取内层对象，否则原样透传
     *
     * @param input
     *     模型给出的原始入参
     * @return 技能入参
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrapInput(Map<String, Object> input) {
        if (Objects.isNull(input)) {
            return Map.of();
        }
        Object wrapped = input.get(PARAM_NAME);
        if (wrapped instanceof Map<?, ?> wrappedMap) {
            return (Map<String, Object>) wrappedMap;
        }
        return input;
    }
}
