package com.yiyibushe.asset;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * 阿里云 OSS 素材服务接口：负责上传、列出、下载与生成分享链接。
 * <p>
 * 业务代码依赖本接口，不直接依赖具体实现，便于替换存储实现或单元测试。
 *
 * @author Witty·Kid Fisher
 */
public interface OssService {

    /**
     * 上传一张素材图片，返回 OSS 公网可访问 URL。
     */
    AssetItem upload(AssetCategory category, MultipartFile file) throws IOException;

    /**
     * 列出指定分类下的全部素材。
     */
    List<AssetItem> list(AssetCategory category);

    /**
     * 下载素材为输入流。
     */
    InputStream download(String key);

    /**
     * 生成指定时长的预签名 URL，用于对外分享。
     */
    String presignUrl(String key, Duration ttl);

    /**
     * 拼接公网可访问的 URL（公开读场景）。如需私有，请改用 presignUrl。
     */
    String publicUrl(String key);
}
