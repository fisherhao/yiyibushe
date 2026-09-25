package com.dayu.yiyibushe.web.interceptor;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import com.dayu.yiyibushe.infra.auth.SessionManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * 登录拦截器：校验会话 Cookie，未登录的接口返回 401、页面跳转登录页。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /** 会话 Cookie 名 */
    public static final String SESSION_COOKIE = "YYBS_SESSION";

    /** 请求属性中的登录主体 key */
    public static final String PRINCIPAL_ATTR = "loginPrincipal";

    @Autowired
    private SessionManager sessionManager;

    /**
     * 校验会话：有效则放行并挂登录主体；接口返 401，页面跳登录页
     *
     * @param request
     *     请求
     * @param response
     *     响应
     * @param handler
     *     处理器
     * @return true 放行，false 拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        LoginPrincipal principal = sessionManager.get(resolveSessionId(request));
        if (Objects.nonNull(principal)) {
            request.setAttribute(PRINCIPAL_ATTR, principal);
            return true;
        }
        if (StringUtilExt.startsWith(request.getRequestURI(), "/api/")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":false,\"errCode\":\""
                    + BizErrorCode.AUTH_CONTEXT_MISSING.getCode()
                    + "\",\"errMsg\":\"" + BizErrorCode.AUTH_CONTEXT_MISSING.getMsg() + "\"}");
        } else {
            response.sendRedirect("/auth/login.html");
        }
        return false;
    }

    /**
     * 从 Cookie 中解析会话 ID
     *
     * @param request
     *     请求
     * @return 会话 ID，没有返回 null
     */
    private String resolveSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (StringUtilExt.equals(SESSION_COOKIE, cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
