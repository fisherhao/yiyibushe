package com.yiyibushe.tryon.impl;

import com.yiyibushe.asset.OssService;
import com.yiyibushe.common.utils.StringUtilsExt;
import com.yiyibushe.tryon.ShareService;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 分享服务实现。
 * <p>
 * 学习项目简化处理：外部 URL 直接透传；OSS 内 key 用预签名 URL（1 小时有效）。
 *
 * @author Witty·Kid Fisher
 */
@Service
public class ShareServiceImpl implements ShareService {

    private final OssService ossService;

    public ShareServiceImpl(OssService ossService) {
        this.ossService = ossService;
    }

    @Override
    public String buildShareUrl(String urlOrKey) {
        if (StringUtilsExt.isBlank(urlOrKey)) {
            throw new IllegalArgumentException("图片地址为空");
        }
        if (urlOrKey.startsWith("http")) {
            return urlOrKey;
        }
        return ossService.presignUrl(urlOrKey, Duration.ofHours(1));
    }
}
