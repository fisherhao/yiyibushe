package com.dayu.yiyibushe.dao.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 说明：用户持久化对象（PO），与 users 表的一条记录对应，只做持久化映射。
 * <p>
 * 全库建表规范：主键 {@code id} 无语义（由发号器生成），业务用户 ID 是 {@code user_id}，
 * 两者并存、互不冲突；时间列统一为 gmt_create / gmt_modify。
 * <p>
 * PO 统一放在 DAO 层的 po 子包中，不单独开一层，避免顶层包过多。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.5
 */
public class UserPO implements Serializable {

    private static final long serialVersionUID = 4729381056293847561L;

    /** 主键 ID（无业务语义，由发号器生成，对应列 id） */
    private Long id;

    /** 业务用户 ID（与主键 id 并存，对应列 user_id） */
    private Long userId;

    /** 登录用户名（唯一） */
    private String username;

    /** 登录密码（PBKDF2 加盐摘要存储，不保存明文） */
    private String password;

    /** 昵称（页面展示用） */
    private String nickname;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /** 创建时间（数据库默认填充） */
    private LocalDateTime gmtCreate;

    /** 更新时间（更新时数据库自动刷新） */
    private LocalDateTime gmtModify;

    /**
     * 无参构造器
     */
    public UserPO() {
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
     * 获取业务用户 ID
     *
     * @return 业务用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置业务用户 ID
     *
     * @param userId
     *               业务用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username
     *                 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码摘要
     *
     * @return 密码摘要串
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码摘要
     *
     * @param password
     *                 密码摘要串
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取昵称
     *
     * @return 昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置昵称
     *
     * @param nickname
     *                 昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取状态
     *
     * @return 状态码
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status
     *               状态码
     */
    public void setStatus(Integer status) {
        this.status = status;
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
