package com.yiyibushe.asset.impl;

import com.yiyibushe.asset.AssetCategory;
import com.yiyibushe.asset.AssetItem;
import com.yiyibushe.asset.OssProperties;
import com.yiyibushe.asset.OssService;
import com.yiyibushe.common.utils.StringUtilsExt;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 阿里云 OSS 素材服务实现。
 *
 * @author Witty·Kid Fisher
 */
@Service
public class OssServiceImpl implements OssService {

    private final OSS ossClient;
    private final OssProperties properties;

    public OssServiceImpl(OSS ossClient, OssProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    @Override
    public AssetItem upload(AssetCategory category, MultipartFile file) throws IOException {
        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "image.png");
        String suffix = StringUtilsExt.substringAfterLast(originalName, ".");
        String key = properties.getAssetPrefix() + category.getPrefix() + "/"
                + UUID.randomUUID().toString().replace("-", "") + suffix;
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectResult result = ossClient.putObject(properties.getBucketName(), key, inputStream, null);
            System.out.println("上传成功: bucket=" + properties.getBucketName()
                    + ", key=" + key + ", etag=" + result.getETag());
        }
        return new AssetItem(key, publicUrl(key), category.name(), originalName, file.getSize());
    }

    @Override
    public List<AssetItem> list(AssetCategory category) {
        String prefix = properties.getAssetPrefix() + category.getPrefix() + "/";
        ListObjectsRequest request = new ListObjectsRequest(properties.getBucketName())
                .withPrefix(prefix)
                .withMaxKeys(200);
        ObjectListing listing = ossClient.listObjects(request);
        List<AssetItem> items = new ArrayList<>();
        for (OSSObjectSummary summary : listing.getObjectSummaries()) {
            if (summary.getKey().endsWith("/")) {
                continue;
            }
            String fileName = StringUtilsExt.substringAfterLast(summary.getKey(), "/");
            items.add(new AssetItem(
                    summary.getKey(),
                    publicUrl(summary.getKey()),
                    category.name(),
                    fileName,
                    summary.getSize()
            ));
        }
        return items;
    }

    @Override
    public InputStream download(String key) {
        OSSObject object = ossClient.getObject(properties.getBucketName(), key);
        return object.getObjectContent();
    }

    @Override
    public String presignUrl(String key, Duration ttl) {
        Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());
        URL url = ossClient.generatePresignedUrl(properties.getBucketName(), key, expiration);
        return url.toString();
    }

    @Override
    public String publicUrl(String key) {
        return "https://" + properties.getBucketName() + "." + properties.getEndpoint() + "/" + key;
    }
}
