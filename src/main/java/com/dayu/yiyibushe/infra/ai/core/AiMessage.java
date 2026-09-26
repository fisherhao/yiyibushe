package com.dayu.yiyibushe.infra.ai.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 说明：AI 消息体，统一不同厂商的消息格式（文本、图片 URL、多模态内容）。
 * <p>
 * 角色固定为 system / user / assistant 三种，与 OpenAI 消息格式对齐，
 * 各厂商连接层负责把本对象转换为对应 API 的请求体。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class AiMessage implements Serializable {

    private static final long serialVersionUID = 7234901856347829104L;

    /** 角色：system / user / assistant */
    private String role;

    /** 文本内容（纯文本场景直接使用） */
    private String content;

    /** 多模态内容片段列表（图文混合场景使用，与 content 二选一） */
    private List<ContentPart> parts;

    /**
     * 说明：多模态内容片段，支持文本与图片 URL 混合。
     *
     * @author Witty·Kid Fisher
     * @version 0.0.1
     */
    public static class ContentPart implements Serializable {

        private static final long serialVersionUID = 3847561928374650192L;

        /** 片段类型：text / image_url */
        private String type;

        /** 文本内容（type=text 时使用） */
        private String text;

        /** 图片 URL（type=image_url 时使用） */
        private String imageUrl;

        /**
         * 无参构造器
         */
        public ContentPart() {
        }

        /**
         * 构造文本片段
         *
         * @param text
         *     文本内容
         * @return 文本片段
         */
        public static ContentPart text(String text) {
            ContentPart part = new ContentPart();
            part.type = "text";
            part.text = text;
            return part;
        }

        /**
         * 构造图片片段
         *
         * @param imageUrl
         *     图片 URL
         * @return 图片片段
         */
        public static ContentPart image(String imageUrl) {
            ContentPart part = new ContentPart();
            part.type = "image_url";
            part.imageUrl = imageUrl;
            return part;
        }

        /**
         * 获取片段类型
         *
         * @return 片段类型
         */
        public String getType() {
            return type;
        }

        /**
         * 获取文本内容（type=text 时使用）
         *
         * @return 文本内容（type=text 时使用）
         */
        public String getText() {
            return text;
        }

        /**
         * 获取图片 URL（type=image_url 时使用）
         *
         * @return 图片 URL（type=image_url 时使用）
         */
        public String getImageUrl() {
            return imageUrl;
        }
    }

    /**
     * 无参构造器
     */
    public AiMessage() {
    }

    /**
     * 构造纯文本消息（静态工厂，避免带参构造器）
     *
     * @param role
     *     角色
     * @param content
     *     文本内容
     * @return 文本消息
     */
    public static AiMessage of(String role, String content) {
        AiMessage message = new AiMessage();
        message.role = role;
        message.content = content;
        return message;
    }

    /**
     * 快捷创建 user 文本消息
     *
     * @param content
     *     文本内容
     * @return user 消息
     */
    public static AiMessage user(String content) {
        return AiMessage.of("user", content);
    }

    /**
     * 快捷创建 system 文本消息
     *
     * @param content
     *     文本内容
     * @return system 消息
     */
    public static AiMessage system(String content) {
        return AiMessage.of("system", content);
    }

    /**
     * 快捷创建 assistant 文本消息
     *
     * @param content
     *     文本内容
     * @return assistant 消息
     */
    public static AiMessage assistant(String content) {
        return AiMessage.of("assistant", content);
    }

    /**
     * 快捷创建多模态 user 消息（图文混合）
     *
     * @param parts
     *     内容片段列表
     * @return user 多模态消息
     */
    public static AiMessage userMultimodal(List<ContentPart> parts) {
        AiMessage msg = new AiMessage();
        msg.role = "user";
        msg.parts = new ArrayList<>(parts);
        return msg;
    }

    /**
     * 获取角色
     *
     * @return 角色
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置角色
     *
     * @param role
                   角色
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 获取文本内容（纯文本场景直接使用）
     *
     * @return 文本内容（纯文本场景直接使用）
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置文本内容（纯文本场景直接使用）
     *
     * @param content
                      文本内容（纯文本场景直接使用）
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取多模态内容片段列表（图文混合场景使用，与 content 二选一）
     *
     * @return 多模态内容片段列表（图文混合场景使用，与 content 二选一）
     */
    public List<ContentPart> getParts() {
        return parts;
    }

    /**
     * 设置多模态内容片段列表（图文混合场景使用，与 content 二选一）
     *
     * @param parts
                    多模态内容片段列表（图文混合场景使用，与 content 二选一）
     */
    public void setParts(List<ContentPart> parts) {
        this.parts = parts;
    }
}
