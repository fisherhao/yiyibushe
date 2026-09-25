package com.dayu.yiyibushe.infra.ai.core;

/**
 * 说明：模型类型枚举，区分不同模态的大模型能力，用于连接层路由与能力校验。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public enum ModelType {

    /** 纯文本对话模型（如 GPT-4o、Claude 3.5、DeepSeek） */
    TEXT_CHAT("text_chat", "文本对话"),

    /** 图片生成模型（如通义万相 wanx、DALL·E、Stable Diffusion） */
    IMAGE_GENERATION("image_generation", "图片生成"),

    /** 多模态对话模型（图文输入，如 GPT-4o、Claude 3.5 Sonnet） */
    MULTIMODAL("multimodal", "多模态对话"),

    /** 虚拟试衣/图像编辑模型（如 wanx-virtualtryon） */
    IMAGE_EDITING("image_editing", "图像编辑/试衣"),

    /** 向量嵌入模型 */
    EMBEDDING("embedding", "向量嵌入");

    private final String code;
    private final String desc;

    ModelType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取类型编码
     *
     * @return 类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型描述
     *
     * @return 类型描述
     */
    public String getDesc() {
        return desc;
    }
}
