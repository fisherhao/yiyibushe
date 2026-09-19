package com.dayu.yiyibushe.domain.tryon;

/**
 * 套装生成请求：用户描述风格、场景与期望效果，并可指定人物图与各部位衣物。
 * <p>
 * 未指定的衣物类别由系统自动选取该用户该分类下最新的一件。
 *
 * @param prompt
 *     风格 / 场景 / 效果提示词
 * @param personKey
 *     人物图 key（可选，缺省取最新人物图；都没有时按提示词文生图生成）
 * @param topKey
 *     上衣 key（可选）
 * @param pantsKey
 *     裤子 key（可选）
 * @param socksKey
 *     袜子 key（可选）
 * @param shoesKey
 *     鞋子 key（可选）
 * @param hatKey
 *     帽子 key（可选）
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record OutfitGenerateRequest(
        String prompt,
        String personKey,
        String topKey,
        String pantsKey,
        String socksKey,
        String shoesKey,
        String hatKey) {
}
