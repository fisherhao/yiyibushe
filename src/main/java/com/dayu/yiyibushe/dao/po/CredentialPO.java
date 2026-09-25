package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 凭证持久化对象（PO），与 ai_credential 表一条记录对应。
 * <p>
 * 厂商模型密钥（app_key/app_secret）的数据库存储载体。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class CredentialPO implements Serializable {

    private static final long serialVersionUID = 8008008008008008008L;

    /** 物理主键 */
    private Long id;

    /** 业务ID */
    private Long credentialId;

    /** 厂商标识：dashscope/qwen/moonshot/hunyuan 等 */
    private String provider;

    /** 应用标识（可空） */
    private String appKey;

    /** 应用密钥 */
    private String appSecret;

    /** 状态：ENABLED/DISABLED */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime gmtCreate;

    /** 更新时间 */
    private LocalDateTime gmtModify;

    /**
     * 获取物理主键
     *
     * @return 物理主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置物理主键
     *
     * @param id
                 物理主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取业务ID
     *
     * @return 业务ID
     */
    public Long getCredentialId() {
        return credentialId;
    }

    /**
     * 设置业务ID
     *
     * @param credentialId
                           业务ID
     */
    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    /**
     * 获取厂商标识
     *
     * @return 厂商标识
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置厂商标识
     *
     * @param provider
                       厂商标识
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 获取应用标识（可空）
     *
     * @return 应用标识（可空）
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * 设置应用标识（可空）
     *
     * @param appKey
                     应用标识（可空）
     */
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    /**
     * 获取应用密钥
     *
     * @return 应用密钥
     */
    public String getAppSecret() {
        return appSecret;
    }

    /**
     * 设置应用密钥
     *
     * @param appSecret
                        应用密钥
     */
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status
                     状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     *
     * @param remark
                     备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
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
                        创建时间
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
                        更新时间
     */
    public void setGmtModify(LocalDateTime gmtModify) {
        this.gmtModify = gmtModify;
    }
}
