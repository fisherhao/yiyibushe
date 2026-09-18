package com.yiyibushe.asset;

/**
 * 素材项：OSS 中的一条图片记录。
 *
 * @param key       OSS 对象 key
 * @param url       公网可访问 URL
 * @param category  素材分类（对应 AssetCategory 枚举名）
 * @param fileName  原始文件名
 * @param size      文件大小（字节）
 *
 * @author Witty·Kid Fisher
 */
public record AssetItem(String key, String url, String category, String fileName, long size) {
}
