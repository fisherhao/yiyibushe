package com.dayu.yiyibushe.infra.config;

import com.dayu.yiyibushe.infra.resource.LocalResourceManager;
import com.dayu.yiyibushe.web.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：跨域、登录拦截器、本地文件资源映射。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private LocalResourceManager resourceManager;

    /**
     * 全局跨域规则
     *
     * @param registry
     *     跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册登录拦截器，放行登录注册页面与认证接口、静态资源
     *
     * @param registry
     *     拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证域页面（仅放行登录、注册、找回密码三页，衣橱页必须登录）
                        "/auth/login.html",
                        "/auth/register.html",
                        "/auth/reset-password.html",
                        // 登录前页面所需的脚本与样式（按业务名精确放行）
                        "/auth/login.js",
                        "/auth/register.js",
                        "/auth/reset-password.js",
                        "/auth/auth.css",
                        // 登录前可调用的认证接口（找回密码无需登录）
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/logout",
                        "/api/auth/reset-password",
                        // 对话演示接口（自然语言触发天气/新闻技能，免登录便于本地验证）
                        "/api/chat/**",
                        // 技能演示首页（单输入框 + 全链路时间线，免登录）
                        "/",
                        "/index.html",
                        // 公共静态资源
                        "/common/**",
                        // 本地素材访问与容器兜底路径
                        "/local-files/**",
                        "/favicon.ico",
                        "/error"
                );
    }

    /**
     * 本地文件资源映射：/local-files/** -> 本地素材目录
     *
     * @param registry
     *     资源注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 与 LocalFileStorageService 同源：都取 LocalResourceManager 的 assets 目录
        registry.addResourceHandler("/local-files/**")
                .addResourceLocations(resourceManager.assetsDir().toUri().toString());
    }
}
