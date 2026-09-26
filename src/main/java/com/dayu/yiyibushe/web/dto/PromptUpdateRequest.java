package com.dayu.yiyibushe.web.dto;

/**
 * 修改提示词内容请求：只允许改 content，version 由数据库自增触发热更新。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class PromptUpdateRequest {

    /** 新提示词内容（支持 {0} 编号占位符） */
    private String content;

    /**
     * 获取提示词内容
     *
     * @return 提示词内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置提示词内容
     *
     * @param content
     *                提示词内容
     */
    public void setContent(String content) {
        this.content = content;
    }
}
