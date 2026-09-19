package com.dayu.yiyibushe.infra.storage;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.domain.asset.AssetCategory;
import com.dayu.yiyibushe.domain.asset.AssetItem;
import com.dayu.yiyibushe.infra.config.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * 阿里云 OSS 存储实现，仅在 storage.type=oss 时启用。
 * <p>
 * 对象路径：{@code {asset-prefix}/{userId}/{category}/{uuid}.png}。
 * 素材列表查询走 asset 表，本类只管文件本体。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Service
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "oss")
public class AliyunOssStorageService implements StorageService {

    private final OSS ossClient;
    private final OssProperties properties;

    /**
     * 构造器
     *
     * @param ossClient
     *                   OSS 客户端
     * @param properties
     *                   OSS 配置
     */
    public AliyunOssStorageService(OSS ossClient, OssProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    /**
     * 上传单个文件到 OSS
     *
     * @param userId
     *                 用户 ID
     * @param category
     *                 素材分类
     * @param file
     *                 上传文件
     * @return 素材信息
     */
    @Override
    public AssetItem upload(Long userId, AssetCategory category, MultipartFile file) throws IOException {
        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "image.png");
        String suffix = StringUtilExt.substringAfterLast(originalName, ".");
        String key = properties.getAssetPrefix() + userId + "/" + category.getPrefix() + "/"
                + UUID.randomUUID().toString().replace("-", "") + suffix;
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectResult result = ossClient.putObject(properties.getBucketName(), key, inputStream, null);
            System.out.println("上传成功: bucket=" + properties.getBucketName()
                    + ", key=" + key + ", etag=" + result.getETag());
        }
        return new AssetItem(null, key, publicUrl(key), category.name(), originalName, file.getSize(), userId);
    }

    /**
     * 下载对象输入流
     *
     * @param key
     *            存储对象 key
     * @return 对象输入流
     */
    @Override
    public InputStream download(String key) {
        OSSObject object = ossClient.getObject(properties.getBucketName(), key);
        return object.getObjectContent();
    }

    /**
     * 生成限时有效的预签名访问 URL
     *
     * @param key
     *            存储对象 key
     * @param ttl
     *            有效期
     * @return 预签名 URL
     */
    @Override
    public String presignUrl(String key, Duration ttl) {
        Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());
        URL url = ossClient.generatePresignedUrl(properties.getBucketName(), key, expiration);
        return url.toString();
    }

    /**
     * 拼接对象的公开访问 URL
     *
     * @param key
     *            存储对象 key
     * @return 公开 URL
     */
    @Override
    public String publicUrl(String key) {
        return "https://" + properties.getBucketName() + "." + properties.getEndpoint() + "/" + key;
    }

    /**
     * 删除对象
     *
     * @param key
     *            存储对象 key
     */
    @Override
    public void delete(String key) {
        ossClient.deleteObject(properties.getBucketName(), key);
    }
}
