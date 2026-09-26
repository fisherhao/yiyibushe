package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.app.service.AuthService;
import com.dayu.yiyibushe.common.ApiResult;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.constant.AiConstants;
import com.dayu.yiyibushe.infra.ai.prompt.PromptStore;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import com.dayu.yiyibushe.infra.auth.SessionManager;
import com.dayu.yiyibushe.web.dto.ChangePasswordRequest;
import com.dayu.yiyibushe.web.dto.LoginRequest;
import com.dayu.yiyibushe.web.dto.RegisterRequest;
import com.dayu.yiyibushe.web.dto.ResetPasswordRequest;
import com.dayu.yiyibushe.web.interceptor.LoginInterceptor;
import com.dayu.yiyibushe.web.interceptor.LoginUtil;
import com.dayu.yiyibushe.web.vo.CurrentUserVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Objects;

/**
 * 认证接口：注册、登录、登出、当前用户、修改密码、找回密码、强制踢登。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 登录态 Cookie 保持时长：7 天（服务端会话另有不活跃超时） */
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PromptStore promptStore;

    /**
     * 注册：注册成功直接创建会话并写 Cookie，免再次登录
     *
     * @param request
     *                 注册请求
     * @param response
     *                 响应（写 Cookie）
     * @return 当前用户信息
     */
    @PostMapping("/register")
    public ApiResult<CurrentUserVO> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        LoginPrincipal principal = authService.register(
                request.getUsername(), request.getPassword(), request.getNickname());
        startSession(principal, response);
        return ApiResult.success(toCurrentUserVO(principal));
    }

    /**
     * 登录：校验通过后写会话 Cookie（记住登录态 7 天）
     *
     * @param request
     *                 登录请求
     * @param response
     *                 响应
     * @return 当前用户信息
     */
    @PostMapping("/login")
    public ApiResult<CurrentUserVO> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginPrincipal principal = authService.login(request.getUsername(), request.getPassword());
        startSession(principal, response);
        return ApiResult.success(toCurrentUserVO(principal));
    }

    /**
     * 登出：销毁会话并让 Cookie 立即过期
     *
     * @param request
     *                 当前请求
     * @param response
     *                 响应
     * @return 成功标记
     */
    @PostMapping("/logout")
    public ApiResult<String> logout(HttpServletRequest request, HttpServletResponse response) {
        CookieReader cookieReader = new CookieReader(request);
        sessionManager.remove(cookieReader.sessionId());
        clearCookie(response);
        return ApiResult.success(promptStore.require(AiConstants.PROMPT_AUTH_LOGOUT_SUCCESS));
    }

    /**
     * 查询当前登录用户
     *
     * @param request
     *                当前请求
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public ApiResult<CurrentUserVO> me(HttpServletRequest request) {
        return ApiResult.success(toCurrentUserVO(LoginUtil.currentUser(request)));
    }

    /**
     * 修改密码：校验原密码后更新，并踢掉该用户全部历史会话，再为当前设备签发新会话
     *
     * @param request
     *                    修改密码请求
     * @param httpRequest
     *                    当前请求（取登录主体）
     * @param response
     *                    响应（重写 Cookie）
     * @return 当前用户信息
     */
    @PostMapping("/change-password")
    public ApiResult<CurrentUserVO> changePassword(@RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        LoginPrincipal current = LoginUtil.currentUser(httpRequest);
        authService.changePassword(current.userId(), request.getOldPassword(), request.getNewPassword());
        // 密码变更后踢掉全部旧会话（含当前设备），再为当前设备重新签发
        sessionManager.kickByUserId(current.userId());
        startSession(current, response);
        return ApiResult.success(toCurrentUserVO(current));
    }

    /**
     * 找回密码：按用户名重置新密码，并踢掉该用户全部会话
     *
     * @param request
     *                 找回密码请求
     * @param response
     *                 响应（清理可能存在的登录 Cookie）
     * @return 成功提示
     */
    @PostMapping("/reset-password")
    public ApiResult<String> resetPassword(@RequestBody ResetPasswordRequest request,
            HttpServletResponse response) {
        Long userId = authService.resetPassword(request.getUsername(), request.getNewPassword());
        sessionManager.kickByUserId(userId);
        clearCookie(response);
        return ApiResult.success(promptStore.require(AiConstants.PROMPT_AUTH_PASSWORD_RESET_SUCCESS));
    }

    /**
     * 强制踢登：按用户 ID 销毁其全部会话，被踢端下次请求即被要求重新登录
     *
     * @param userId
     *               目标用户 ID
     * @return 被销毁的会话数量
     */
    @PostMapping("/kickout")
    public ApiResult<Integer> kickout(@RequestParam("userId") Long userId) {
        return ApiResult.success(sessionManager.kickByUserId(userId));
    }

    /**
     * 创建会话并写 Cookie
     *
     * @param principal
     *                  登录主体
     * @param response
     *                  响应
     */
    private void startSession(LoginPrincipal principal, HttpServletResponse response) {
        String sessionId = sessionManager.create(principal);
        ResponseCookie cookie = ResponseCookie.from(LoginInterceptor.SESSION_COOKIE, sessionId)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(SESSION_TTL)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 让登录 Cookie 立即过期
     *
     * @param response
     *                 响应
     */
    private void clearCookie(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from(LoginInterceptor.SESSION_COOKIE, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }

    /**
     * 转当前用户 VO
     *
     * @param principal
     *                  登录主体
     * @return 当前用户 VO
     */
    private CurrentUserVO toCurrentUserVO(LoginPrincipal principal) {
        CurrentUserVO vo = new CurrentUserVO();
        vo.setUserId(principal.userId());
        vo.setUsername(principal.username());
        vo.setNickname(principal.nickname());
        return vo;
    }

    /**
     * Cookie 读取器（小工具，统一处理 Cookie 数组为 null 的情况）
     */
    private static class CookieReader {

        private final HttpServletRequest request;

        /**
         * 构造器
         *
         * @param request
         *                当前请求
         */
        CookieReader(HttpServletRequest request) {
            this.request = request;
        }

        /**
         * 读取会话 Cookie 的值
         *
         * @return 会话 ID，不存在返回 null
         */
        String sessionId() {
            Cookie[] cookies = request.getCookies();
            if (Objects.isNull(cookies)) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if (StringUtilExt.equals(LoginInterceptor.SESSION_COOKIE, cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        }
    }
}
