package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.app.ai.agent.AssistantChatAgent;
import com.dayu.yiyibushe.infra.ai.trace.ExecutionTrace;
import com.dayu.yiyibushe.infra.ai.trace.TracePhase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * 统一对话接口：自然语言驱动，由大模型自行路由技能。
 * <p>
 * 这里只把用户原话交给 {@link AssistantChatAgent}：问天气模型自行命中
 * current-weather 技能，问科技新闻命中 daily-tech-news 技能，一句话问两个
 * 就两个都调；都不命中则返回超出范畴固定话术。用户不需要选择技能或端点。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    /** 统一对话 Agent */
    @Autowired
    private AssistantChatAgent assistantChatAgent;

    /**
     * 对话：天气、新闻、超出范畴都走这一个入口，技能由模型路由
     *
     * @param request
     *                请求体：message 用户消息，如 {"message":"今天有什么科技新闻"}
     * @return 最终回答 + 命中技能 + 全链路步骤
     */
    @PostMapping("/message")
    public ChatResponse chat(@RequestBody Map<String, String> request) {
        String message = Objects.requireNonNullElse(request.get("message"), "");

        ExecutionTrace trace = new ExecutionTrace();
        trace.step(TracePhase.REQUEST,
                "HTTP 请求进入 ChatController.chat()，收到用户消息：\"{0}\"", message);

        String answer = assistantChatAgent.chat(message, trace);

        trace.step(TracePhase.RESPONSE, "ChatResponse 组装完成并返回页面");
        return new ChatResponse(answer, trace.getHitSkills(), trace.getHitTools(),
                trace.getSteps());
    }
}
