package com.yiyibushe.tryon;

import java.util.List;

/**
 * 文生图请求：用户输入文字描述，可附 OSS 中素材的 key 列表作为参考。
 *
 * @param prompt        文字描述
 * @param refAssetKeys  参考素材 OSS key 列表（可选）
 *
 * @author Witty·Kid Fisher
 */
public record TextToImageRequest(
        String prompt,
        List<String> refAssetKeys) {
}
