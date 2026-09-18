package com.yiyibushe.tryon;

/**
 * 虚拟试衣请求：人物形象图 key + 各部位服装图 key。
 * <p>
 * 各部位支持按 上衣 -> 裤子 -> 鞋子 -> 帽子 顺序链式试穿。
 * 至少需要一件服装素材。
 *
 * @param personKey 人物形象图 OSS key
 * @param topKey    上衣 OSS key（可选）
 * @param pantsKey  裤子 OSS key（可选）
 * @param shoesKey  鞋子 OSS key（可选）
 * @param hatKey    帽子 OSS key（可选）
 *
 * @author Witty·Kid Fisher
 */
public record VirtualTryOnRequest(
        String personKey,
        String topKey,
        String pantsKey,
        String shoesKey,
        String hatKey) {
}
