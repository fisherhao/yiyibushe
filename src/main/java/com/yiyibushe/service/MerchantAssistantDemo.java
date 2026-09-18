package com.yiyibushe.service;

import com.alibaba.fastjson2.JSON;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * 商家智能助手演示入口：通过 DeepSeek 模型回答商家问题。
 *
 * @author Witty·Kid Fisher
 * @version v 0.1 2026年09月16日 星期三 15:53
 */
public class MerchantAssistantDemo {

    // 从当前运行环境读取 DeepSeek API Key，不再硬编码
    private static final String API_KEY = System.getenv("DEEPSEEK_API_KEY");

    public static void main(String[] args) {
        if (Objects.isNull(API_KEY) || API_KEY.isBlank()) {
            System.err.println("请先设置环境变量 DEEPSEEK_API_KEY");
            System.exit(1);
        }

        // DeepSeek 最新模型：V4.1 Flash，模型名 deepseek-flash，OpenAI 兼容端点
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(API_KEY)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-flash")
                .stream(true)
                .build();

        HarnessAgent agent = HarnessAgent.builder()
                .name("merchant-assistant")
                .sysPrompt("你是商家智能助手，帮助商家分析店铺数据。")
                .model(model)
                .workspace(Paths.get(".agentscope/workspace"))
                .build();

        // 商家小蜜的调用
        RuntimeContext merchantContext = RuntimeContext.builder()
                .userId("tenant_merchant_001") // 商家租户
                .sessionId("session_merchant_abc")
                .build();

        // 发起调用，并打印大模型回复的文本
        Msg reply = agent.call(new UserMessage("我的店铺数据如何？"), merchantContext).block();
        System.out.println("==================================");
        System.out.println(JSON.toJSONString(reply));
        System.out.println("==================================");
        System.out.println("商家小蜜回复：" + reply.getTextContent());
    }
}
