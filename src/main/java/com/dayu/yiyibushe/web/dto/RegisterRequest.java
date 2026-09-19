package com.dayu.yiyibushe.web.dto;

/**
 * 注册请求。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public class RegisterRequest {

    /** 用户名 */
    private String username;

    /** 明文密码 */
    private String password;

    /** 昵称（可选） */
    private String nickname;

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
     * 获取明文密码
     *
     * @return 明文密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置明文密码
     *
     * @param password
     *     明文密码
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
     *     昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
