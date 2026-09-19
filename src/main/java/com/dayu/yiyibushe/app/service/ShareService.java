package com.dayu.yiyibushe.app.service;

/**
 * 分享服务接口：把生成的图片 URL 包装成可在浏览器直接访问的链接。
 *
 * @author Witty·Kid Fisher
 */
public interface ShareService {

    /**
     * 如果图片是 OSS 中的 key，则生成预签名 URL；
     * 如果是模型直链（外部 URL），则直接返回。
     */
    String buildShareUrl(String urlOrKey);
}
