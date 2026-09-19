package com.dayu.yiyibushe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口。
 *
 * @author Witty·Kid Fisher
 */
@SpringBootApplication
public class ChatApplication {

    /**
     * 主方法：启动 Spring Boot 应用
     *
     * @param args
     *     启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
