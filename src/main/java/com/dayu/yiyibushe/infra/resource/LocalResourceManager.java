package com.dayu.yiyibushe.infra.resource;

import jakarta.annotation.PostConstruct;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 本地资源管理器：所有文件型资源的统一入口。
 * <p>
 * 目录约定（根目录由 {@code local-resource.dir} 配置，默认 {@code local-resource}）：
 * <pre>
 * local-resource/
 *   assets/  上传的图片资源（{userId}/{category}/{uuid}.ext）
 * </pre>
 * 本地存储只依赖本类取路径、读写文件；切换 OSS 时上层代码不感知。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class LocalResourceManager {

    private static final Logger log = LogUtilExt.getLogger(LocalResourceManager.class);

    /** 素材目录名 */
    public static final String ASSETS_DIR = "assets";

    @Autowired
    private LocalResourceProperties properties;

    /** 资源根目录（绝对路径） */
    private Path rootDir;

    /** 素材目录 */
    private Path assetsDir;

    /**
     * 初始化并确保根目录与 assets 目录存在
     */
    @PostConstruct
    public void init() {
        this.rootDir = properties.getDir().toAbsolutePath().normalize();
        this.assetsDir = rootDir.resolve(ASSETS_DIR);
        try {
            Files.createDirectories(assetsDir);
        } catch (IOException e) {
            throw new UncheckedIOException("初始化本地资源目录失败: " + rootDir, e);
        }
        LogUtilExt.info(log, "[LocalResource] 资源目录: {0}", rootDir);
    }

    /**
     * 获取素材目录
     *
     * @return assets 目录路径
     */
    public Path assetsDir() {
        return assetsDir;
    }

    /**
     * 按素材 key 解析物理文件
     *
     * @param key
     *     素材 key（如 1/top/xxx.png）
     * @return 物理文件路径
     */
    public Path assetFile(String key) {
        return assetsDir.resolve(key);
    }

    /**
     * 读取文本文件（UTF-8）
     *
     * @param file
     *     文件路径
     * @return 文件内容
     */
    public String readString(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("读取本地资源失败: " + file, e);
        }
    }

    /**
     * 写入文本文件（UTF-8），自动创建父目录
     *
     * @param file
     *     文件路径
     * @param content
     *     文件内容
     */
    public void writeString(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("写入本地资源失败: " + file, e);
        }
    }

    /**
     * 删除指定相对路径的文件
     *
     * @param relativePath
     *     相对资源根目录的路径
     * @return true 表示原文件存在并被删除
     */
    public boolean deleteFile(String relativePath) {
        try {
            return Files.deleteIfExists(rootDir.resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("删除本地资源失败: " + relativePath, e);
        }
    }

    /**
     * 递归删除目录
     *
     * @param dir
     *     待删除目录
     */
    public void deleteDirectory(Path dir) {
        if (Files.notExists(dir)) {
            return;
        }
        try (Stream<Path> pathStream = Files.walk(dir)) {
            pathStream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new UncheckedIOException("删除目录失败: " + path, e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("遍历目录失败: " + dir, e);
        }
    }
}