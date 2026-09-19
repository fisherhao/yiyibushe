package com.dayu.yiyibushe.app.ai.skill;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.domain.ai.skill.AiSkill;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 穿搭建议技能：领域技能 {@link AiSkill} 的内存演示实现。
 * <p>
 * 当前为基础版：按入参拼装搭配建议，不调用大模型、不访问数据库，
 * 保证应用在无外部依赖时也能启动。后续可替换为调用 AiPlatformService 的真实实现。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class OutfitSkill implements AiSkill {

    /** 技能名称（Agent/function calling 通过该名称调用） */
    private static final String SKILL_NAME = "outfit-advice";

    /**
     * 获取技能名称
     *
     * @return 技能名称
     */
    @Override
    public String getName() {
        return SKILL_NAME;
    }

    /**
     * 获取技能描述（供 Agent 判断何时调用本技能）
     *
     * @return 技能描述
     */
    @Override
    public String getDescription() {
        return "根据上装、下装与场景，生成一套穿搭建议";
    }

    /**
     * 执行技能，拼装穿搭建议
     *
     * @param parameters
     *     入参：top 上装、pants 下装、scene 场景
     * @return 穿搭建议文本
     */
    @Override
    public String invoke(Map<String, Object> parameters) {
        String top = StringUtilExt.defaultIfBlank(asString(parameters.get("top")), "未知上装");
        String pants = StringUtilExt.defaultIfBlank(asString(parameters.get("pants")), "未知下装");
        String scene = StringUtilExt.defaultIfBlank(asString(parameters.get("scene")), "日常");
        return "推荐搭配：" + top + " + " + pants + "，适合「" + scene + "」场景";
    }

    /** 入参对象转字符串；null 返回 null 交由调用方给默认值。 */
    private String asString(Object value) {
        return Objects.isNull(value) ? null : String.valueOf(value);
    }
}
