package com.dayu.yiyibushe.app.service.impl;

import com.dayu.yiyibushe.app.service.ShareService;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.storage.StorageService;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 分享服务实现。
 * <p>
 * 链接处理：外部 URL 直接透传；OSS 内 key 用预签名 URL（1 小时有效）。
 *
 * @author Witty·Kid Fisher
 */
@Service
public class ShareServiceImpl implements ShareService {

    private final StorageService ossService;

    /**
     * 构造器
     *
     * @param ossService
     *     存储服务
     */
    public ShareServiceImpl(StorageService ossService) {
        this.ossService = ossService;
    }

    /**
     * 构建分享链接：外链直接透传，存储 key 生成 1 小时有效的预签名 URL
     *
     * @param urlOrKey
     *     图片外链或存储 key
     * @return 可访问的分享 URL
     */
    @Override
    public String buildShareUrl(String urlOrKey) {
        if (StringUtilExt.isBlank(urlOrKey)) {
            throw new BizException(ParamErrorCode.PARAM_INVALID);
        }
        if (StringUtilExt.startsWith(urlOrKey, "http")) {
            return urlOrKey;
        }
        return ossService.presignUrl(urlOrKey, Duration.ofHours(1));
    }
}
