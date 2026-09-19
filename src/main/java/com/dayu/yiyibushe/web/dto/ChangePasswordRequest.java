package com.dayu.yiyibushe.web.dto;

/**
 * 修改密码请求（登录态下使用）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ChangePasswordRequest {

    /** 原密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;

    /**
     * 获取原密码
     *
     * @return 原密码
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * 设置原密码
     *
     * @param oldPassword
     *     原密码
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
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
