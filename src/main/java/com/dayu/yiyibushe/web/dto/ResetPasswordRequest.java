package com.dayu.yiyibushe.web.dto;

/**
 * 找回密码请求（未登录可访问；生产环境需先完成邮箱/短信核验）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ResetPasswordRequest {

    /** 用户名 */
    private String username;

    /** 新密码 */
    private String newPassword;

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
     * 获取新密码
     *
     * @return 新密码
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * 设置新密码
     *
     * @param newPassword
     *     新密码
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
