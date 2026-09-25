package com.dayu.yiyibushe.web.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 说明：用户响应 DTO，对外返回用户数据，不暴露 password 等敏感字段。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public class UserResponseDTO implements Serializable {

    private static final long serialVersionUID = 2938475610293847561L;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /** 注册时间 */
    private LocalDateTime registerTime;

    /**
     * 无参构造器
     */
    public UserResponseDTO() {
    }

    /**
     * 获取用户 ID
     *
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户 ID
     *
     * @param userId
                     用户 ID
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
                       用户名
     */
    public void setUsername(String username) {
        this.username = username;
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
                       昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status
                     状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取注册时间
     *
     * @return 注册时间
     */
    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    /**
     * 设置注册时间
     *
     * @param registerTime
                           注册时间
     */
    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }
}
