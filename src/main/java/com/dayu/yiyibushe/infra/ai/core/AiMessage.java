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
         * Getter method for property <tt>type</tt>.
         *
         * @return property value of type
         */
        public String getType() {
            return type;
        }

        /**
         * Getter method for property <tt>text</tt>.
         *
         * @return property value of text
         */
        public String getText() {
            return text;
        }

        /**
         * Getter method for property <tt>imageUrl</tt>.
         *
         * @return property value of imageUrl
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
     * 构造纯文本消息
     *
     * @param role
     *     角色
     * @param content
     *     文本内容
     */
    public AiMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 快捷创建 user 文本消息
     *
     * @param content
     *     文本内容
     * @return user 消息
     */
    public static AiMessage user(String content) {
        return new AiMessage("user", content);
    }

    /**
     * 快捷创建 system 文本消息
     *
     * @param content
     *     文本内容
     * @return system 消息
     */
    public static AiMessage system(String content) {
        return new AiMessage("system", content);
    }

    /**
     * 快捷创建 assistant 文本消息
     *
     * @param content
     *     文本内容
     * @return assistant 消息
     */
    public static AiMessage assistant(String content) {
        return new AiMessage("assistant", content);
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
     * Getter method for property <tt>role</tt>.
     *
     * @return property value of role
     */
    public String getRole() {
        return role;
    }

    /**
     * Setter method for property <tt>role</tt>.
     *
     * @param role
     *     value to be assigned to property role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Getter method for property <tt>content</tt>.
     *
     * @return property value of content
     */
    public String getContent() {
        return content;
    }

    /**
     * Setter method for property <tt>content</tt>.
     *
     * @param content
     *     value to be assigned to property content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Getter method for property <tt>parts</tt>.
     *
     * @return property value of parts
     */
    public List<ContentPart> getParts() {
        return parts;
    }

    /**
     * Setter method for property <tt>parts</tt>.
     *
     * @param parts
     *     value to be assigned to property parts
     */
    public void setParts(List<ContentPart> parts) {
        this.parts = parts;
    }
}
