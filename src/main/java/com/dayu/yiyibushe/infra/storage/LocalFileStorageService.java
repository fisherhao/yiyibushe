package com.dayu.yiyibushe.infra.storage;

import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.domain.asset.AssetCategory;
import com.dayu.yiyibushe.domain.asset.AssetItem;
import com.dayu.yiyibushe.infra.resource.LocalResourceManager;
import jakarta.annotation.PostConstruct;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * 本地文件夹存储实现，测试阶段默认启用，不依赖任何云服务。
 * <p>
 * 文件路径：{@code local-resource/assets/{userId}/{category}/{uuid}.png}。
 * 通过站点内 {@code /local-files/**} 路径对外访问；物理目录统一由
 * {@link LocalResourceManager} 管理，保证「落盘目录 ↔ 资源映射 ↔ URL」同源。
 * 素材列表查询走 asset 表，本类只管文件本体。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Service
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements StorageService {

    private static final Logger log = LogUtilExt.getLogger(LocalFileStorageService.class);

    /** 本地文件的 URL 访问前缀（与 WebConfig 资源映射对应） */
    private static final String URL_PREFIX = "/local-files/";

    @Autowired
    private LocalResourceManager resourceManager;

    /** 素材根目录 */
    private Path assetRoot;

    /**
     * 初始化素材根目录
     */
    @PostConstruct
    public void init() {
        this.assetRoot = resourceManager.assetsDir();
        LogUtilExt.info(log, "[Storage] 使用本地文件夹存储: {0}", assetRoot);
    }

    /**
     * 上传单个文件到本地素材目录
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
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("image.png");
        String suffix = StringUtilExt.substringAfterLast(originalName, ".");
        String extension = StringUtilExt.isBlank(suffix) ? ".png" : "." + suffix;
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String key = buildKey(userId, category, fileName);
        Path target = resolveKey(key);
        Files.createDirectories(target.getParent());
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target);
        }
        return new AssetItem(null, key, publicUrl(key), category.name(), originalName, file.getSize(), userId);
    }

    /**
     * 打开本地文件输入流
     *
     * @param key
     *            存储对象 key
     * @return 文件输入流
     */
    @Override
    public InputStream download(String key) {
        try {
            return Files.newInputStream(resolveKey(key));
        } catch (IOException e) {
            throw new UncheckedIOException("读取本地文件失败: " + key, e);
        }
    }

    /**
     * 本地文件可直接访问，预签名 URL 退化为常规 URL
     *
     * @param key
     *            存储对象 key
     * @param ttl
     *            有效期（忽略）
     * @return 常规访问 URL
     */
    @Override
    public String presignUrl(String key, Duration ttl) {
        // 本地文件本身即可访问，直接返回常规 URL
        return publicUrl(key);
    }

    /**
     * 拼接本地文件的站点访问 URL
     *
     * @param key
     *            存储对象 key
     * @return 访问 URL
     */
    @Override
    public String publicUrl(String key) {
        return URL_PREFIX + key;
    }

    /**
     * 删除本地文件（不存在不报错）
     *
     * @param key
     *            存储对象 key
     */
    @Override
    public void delete(String key) throws IOException {
        Files.deleteIfExists(resolveKey(key));
    }

    /**
     * 拼接存储对象 key
     *
     * @param userId
     *                 用户 ID
     * @param category
     *                 分类
     * @param fileName
     *                 文件名
     * @return key
     */
    private String buildKey(Long userId, AssetCategory category, String fileName) {
        return Paths.get(String.valueOf(userId), category.getPrefix(), fileName).toString();
    }

    /**
     * key 转物理文件路径
     *
     * @param key
     *            存储对象 key
     * @return 物理路径
     */
    private Path resolveKey(String key) {
        return assetRoot.resolve(key);
    }
}