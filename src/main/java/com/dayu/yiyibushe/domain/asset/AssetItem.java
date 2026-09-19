package com.dayu.yiyibushe.domain.asset;

/**
 * 素材项：一张图片的完整记录（asset 表一行 + 文件本体在存储介质中）。
 *
 * @param assetId
 *                 业务素材 ID（18 位，asset 表发号，与库记录一一对应）
 * @param key
 *                 存储对象 key（本地相对路径或 OSS key，未来对接 OSS 直接复用）
 * @param url
 *                 可访问 URL（本地为站点相对路径，云端为公网 URL）
 * @param category
 *                 素材分类（对应 AssetCategory 枚举名）
 * @param fileName
 *                 原始文件名
 * @param size
 *                 文件大小（字节，未来统计用量用）
 * @param userId
 *                 所属用户 ID
 *
 * @author Witty·Kid Fisher
 * @version 0.0.5
 */
public record AssetItem(Long assetId, String key, String url, String category, String fileName, long size,
        Long userId) {
}
