package com.dayu.yiyibushe.domain.ai.skill;

import java.util.Map;

/**
 * AI 技能接口：领域层对「可被 Agent 复用的原子业务能力」的抽象。
 * <p>
 * Skill 与 Agent 的区别：Agent 负责思考与编排（决定做什么），Skill 负责执行
 * 一个确定动作（知道怎么做），例如「生成一套穿搭建议」。接口只描述契约，
 * 不依赖大模型连接、HTTP 等基础设施，具体实现放在 app 层（依赖倒置）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface AiSkill {

    /**
     * 获取技能名称（Agent 据此选择技能，需唯一）
     *
     * @return 技能名称
     */
    String getName();

    /**
     * 获取技能描述（供大模型 function calling 判断何时调用）
     *
     * @return 技能描述
     */
    String getDescription();

    /**
     * 执行技能
     *
     * @param parameters
     *     入参集合（key 为参数名）
     * @return 执行结果文本
     */
    String invoke(Map<String, Object> parameters);
}
