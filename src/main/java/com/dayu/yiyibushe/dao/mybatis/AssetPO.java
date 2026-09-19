package com.dayu.yiyibushe.dao.mybatis;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 说明：图片素材持久化对象（asset 表）。
 * <p>
 * 图片文件存本地（local-resource/assets），访问链接与归属关系落本表：
 * user_id 绑定归属人，category 绑定衣物类别（人物图/帽子/上衣/裤子/袜子/鞋子）。
 * 主键 id 无语义、由发号器生成（非自增），业务素材 ID 为 asset_id，两者并存；
 * gmt_create 由数据库默认填充，gmt_modify 更新时数据库自动刷新。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class AssetPO implements Serializable {

    private static final long serialVersionUID = 5276193048265174932L;

    /** 主键 ID（无语义，由发号器生成，对应列 id） */
    private Long id;

    /** 业务素材 ID（与主键 id 并存，对应列 asset_id） */
    private Long assetId;

    /** 归属用户 ID（业务外键，关联 users.user_id） */
    private Long userId;

    /** 素材分类：AVATAR/TOP/PANTS/SOCKS/SHOES/HAT */
    private String category;

    /** 访问链接（本地为站点相对路径，云端为公网 URL） */
    private String url;

    /** 存储对象 key（本地相对路径或 OSS key） */
    private String objectKey;

    /** 图片原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 图片过期时间（上传时间 + 7 天，到期后列表不再展示） */
    private LocalDateTime gmtExpire;

    /** 创建时间（数据库默认填充） */
    private LocalDateTime gmtCreate;

    /** 更新时间（更新时数据库自动刷新） */
    private LocalDateTime gmtModify;

    /**
     * 无参构造器（反序列化使用）
     */
    public AssetPO() {
    }

    /**
     * 获取主键 ID
     *
     * @return 主键 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键 ID
     *
     * @param id
     *           主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取业务素材 ID
     *
     * @return 业务素材 ID
     */
    public Long getAssetId() {
        return assetId;
    }

    /**
     * 设置业务素材 ID
     *
     * @param assetId
     *                业务素材 ID
     */
    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    /**
     * 获取归属用户 ID
     *
     * @return 归属用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置归属用户 ID
     *
     * @param userId
     *               归属用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取素材分类
     *
     * @return 分类名
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置素材分类
     *
     * @param category
     *                 分类名
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取访问链接
     *
     * @return 访问链接
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置访问链接
     *
     * @param url
     *            访问链接
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取存储对象 key
     *
     * @return 存储 key
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置存储对象 key
     *
     * @param objectKey
     *                  存储 key
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 获取图片原始文件名
     *
     * @return 原始文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置图片原始文件名
     *
     * @param fileName
     *                 原始文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 获取文件大小
     *
     * @return 文件大小（字节）
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 设置文件大小
     *
     * @param fileSize
     *                 文件大小（字节）
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 获取图片过期时间
     *
     * @return 图片过期时间
     */
    public LocalDateTime getGmtExpire() {
        return gmtExpire;
    }

    /**
     * 设置图片过期时间
     *
     * @param gmtExpire
     *                  图片过期时间
     */
    public void setGmtExpire(LocalDateTime gmtExpire) {
        this.gmtExpire = gmtExpire;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 设置创建时间
     *
     * @param gmtCreate
     *                  创建时间
     */
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 获取更新时间
     *
     * @return 更新时间
     */
    public LocalDateTime getGmtModify() {
        return gmtModify;
    }

    /**
     * 设置更新时间
     *
     * @param gmtModify
     *                  更新时间
     */
    public void setGmtModify(LocalDateTime gmtModify) {
        this.gmtModify = gmtModify;
    }
}
