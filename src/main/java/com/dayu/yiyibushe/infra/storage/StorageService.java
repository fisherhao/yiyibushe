package com.dayu.yiyibushe.infra.storage;

import com.dayu.yiyibushe.domain.asset.AssetCategory;
import com.dayu.yiyibushe.domain.asset.AssetItem;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/**
 * 文件存储服务接口：负责图片文件本体的上传、下载、删除与访问 URL 生成。
 * <p>
 * 素材的归属与链接记录统一落 asset 表（见 AssetMybatisMapper），
 * 列表查询走数据库，本接口只管文件本体，不感知底层是本地文件夹还是阿里云 OSS。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
public interface StorageService {

    /**
     * 上传一张图片（只落文件与返回文件信息，链接落库由素材服务完成）
     *
     * @param userId
     *     所属用户 ID
     * @param category
     *     素材分类
     * @param file
     *     图片文件
     * @return 素材项（assetId 未发号，为 null，由素材服务落库时填充）
     * @throws IOException 读写异常
     */
    AssetItem upload(Long userId, AssetCategory category, MultipartFile file) throws IOException;

    /**
     * 按 key 下载文件
     *
     * @param key
     *     存储对象 key
     * @return 文件输入流
     */
    InputStream download(String key);

    /**
     * 生成指定时长的临时访问 URL（分享场景）
     *
     * @param key
     *     存储对象 key
     * @param ttl
     *     有效时长
     * @return 访问 URL
     */
    String presignUrl(String key, Duration ttl);

    /**
     * 拼接常规访问 URL
     *
     * @param key
     *     存储对象 key
     * @return 访问 URL
     */
    String publicUrl(String key);

    /**
     * 按 key 删除文件
     *
     * @param key
     *     存储对象 key
     * @throws IOException 读写异常
     */
    void delete(String key) throws IOException;
}
