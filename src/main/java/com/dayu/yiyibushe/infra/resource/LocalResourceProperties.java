package com.dayu.yiyibushe.infra.resource;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * 本地临时资源配置（对应 application.properties 中 local-resource.* 前缀）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@ConfigurationProperties(prefix = "local-resource")
public class LocalResourceProperties {

    /**
     * 临时资源根目录（相对项目运行目录）。
     * 本地资源统一落在此目录：assets/ 存放上传图片。
     */
    private Path dir = Path.of("local-resource");

    /**
     * 获取临时资源根目录
     *
     * @return 根目录
     */
    public Path getDir() {
        return dir;
    }

    /**
     * 设置临时资源根目录
     *
     * @param dir
     *     根目录
     */
    public void setDir(Path dir) {
        this.dir = dir;
    }
}
