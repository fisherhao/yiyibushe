package com.yiyibushe.asset;

import java.util.Objects;

/**
 * 素材分类：把人物图、帽子、上衣、裤子、鞋子分开存放到 OSS 不同前缀目录下，
 * 便于按类型检索。
 *
 * @author Witty·Kid Fisher
 */
public enum AssetCategory {

    /** 人物形象图（头像或全身照） */
    AVATAR("avatar", "人物图"),

    /** 帽子 */
    HAT("hat", "帽子"),

    /** 上衣 */
    TOP("top", "上衣"),

    /** 裤子 */
    PANTS("pants", "裤子"),

    /** 鞋子 */
    SHOES("shoes", "鞋子");

    private final String prefix;
    private final String label;

    AssetCategory(String prefix, String label) {
        this.prefix = prefix;
        this.label = label;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 大小写不敏感地解析枚举。
     */
    public static AssetCategory parse(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalArgumentException("素材类别不能为空");
        }
        for (AssetCategory category : values()) {
            if (category.name().equalsIgnoreCase(value) || category.prefix.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知素材类别: " + value);
    }
}
