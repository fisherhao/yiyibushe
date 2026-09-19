package com.dayu.yiyibushe.web.interceptor;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录上下文工具：从请求属性中获取当前登录主体。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public final class LoginUtil {

    /**
     * 私有构造器：工具类不允许实例化
     */
    private LoginUtil() {
    }

    /**
     * 获取当前登录主体
     *
     * @param request
     *     当前请求
     * @return 登录主体
     */
    public static LoginPrincipal currentUser(HttpServletRequest request) {
        Object principal = request.getAttribute(LoginInterceptor.PRINCIPAL_ATTR);
        if (principal instanceof LoginPrincipal loginPrincipal) {
            return loginPrincipal;
        }
        throw new BizException(BizErrorCode.AUTH_CONTEXT_MISSING);
    }
}
