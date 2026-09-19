package com.dayu.yiyibushe.domain.asset;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;

/**
 * 素材分类：把人物图、帽子、上衣、裤子、袜子、鞋子分开存放到不同前缀目录下，
 * 便于按类型检索，也便于按类做数量限制。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
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

    /** 袜子 */
    SOCKS("socks", "袜子"),

    /** 鞋子 */
    SHOES("shoes", "鞋子");

    /** 存储路径前缀 */
    private final String prefix;

    /** 页面展示名 */
    private final String label;

    /**
     * 构造器
     *
     * @param prefix
     *     存储路径前缀
     * @param label
     *     页面展示名
     */
    AssetCategory(String prefix, String label) {
        this.prefix = prefix;
        this.label = label;
    }

    /**
     * 获取存储路径前缀
     *
     * @return 前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 获取页面展示名
     *
     * @return 展示名
     */
    public String getLabel() {
        return label;
    }

    /**
     * 大小写不敏感地解析枚举
     *
     * @param value
     *     分类名或前缀
     * @return 素材分类
     */
    public static AssetCategory parse(String value) {
        if (StringUtilExt.isBlank(value)) {
            throw new BizException(ParamErrorCode.PARAM_INVALID);
        }
        for (AssetCategory category : values()) {
            if (StringUtilExt.equalsIgnoreCase(category.name(), value)
                    || StringUtilExt.equalsIgnoreCase(category.prefix, value)) {
                return category;
            }
        }
        throw new BizException(BizErrorCode.ASSET_CATEGORY_UNKNOWN);
    }
}
