package com.dayu.yiyibushe.web.vo;

/**
 * 当前登录用户信息。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public class CurrentUserVO {

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /**
     * 无参构造器
     */
    public CurrentUserVO() {
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
     *     用户 ID
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
     *     用户名
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
     *     昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
