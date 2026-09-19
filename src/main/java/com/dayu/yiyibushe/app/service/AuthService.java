package com.dayu.yiyibushe.app.service;

import com.dayu.yiyibushe.infra.auth.LoginPrincipal;

/**
 * 认证服务接口：本地注册、登录。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface AuthService {

    /**
     * 注册新用户
     *
     * @param username
     *                    用户名
     * @param rawPassword
     *                    明文密码
     * @param nickname
     *                    昵称（为空时取用户名）
     * @return 登录主体
     */
    LoginPrincipal register(String username, String rawPassword, String nickname);

    /**
     * 登录校验
     *
     * @param username
     *                    用户名
     * @param rawPassword
     *                    明文密码
     * @return 登录主体
     */
    LoginPrincipal login(String username, String rawPassword);

    /**
     * 修改密码：校验原密码后更新为新密码
     *
     * @param userId
     *                       用户 ID
     * @param oldRawPassword
     *                       原密码
     * @param newRawPassword
     *                       新密码
     */
    void changePassword(Long userId, String oldRawPassword, String newRawPassword);

    /**
     * 找回密码：暂无邮箱/短信通道，直接按用户名重置。
     * 生产环境必须先完成邮箱或短信身份核验。
     *
     * @param username
     *                       用户名
     * @param newRawPassword
     *                       新密码
     * @return 被重置用户的 ID
     */
    Long resetPassword(String username, String newRawPassword);
}
