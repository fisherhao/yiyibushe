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
     * Getter method for property <tt>userId</tt>.
     *
     * @return property value of userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Setter method for property <tt>userId</tt>.
     *
     * @param userId
     *     value to be assigned to property userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Getter method for property <tt>username</tt>.
     *
     * @return property value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter method for property <tt>username</tt>.
     *
     * @param username
     *     value to be assigned to property username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter method for property <tt>nickname</tt>.
     *
     * @return property value of nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Setter method for property <tt>nickname</tt>.
     *
     * @param nickname
     *     value to be assigned to property nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Getter method for property <tt>status</tt>.
     *
     * @return property value of status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     *
     * @param status
     *     value to be assigned to property status
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * Getter method for property <tt>registerTime</tt>.
     *
     * @return property value of registerTime
     */
    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    /**
     * Setter method for property <tt>registerTime</tt>.
     *
     * @param registerTime
     *     value to be assigned to property registerTime
     */
    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }
}
