package com.dayu.yiyibushe.infra.auth;

/**
 * 登录主体：会话中保存的最小用户信息。
 *
 * @param userId
 *     用户 ID
 * @param username
 *     用户名
 * @param nickname
 *     昵称
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public record LoginPrincipal(Long userId, String username, String nickname) {
}
