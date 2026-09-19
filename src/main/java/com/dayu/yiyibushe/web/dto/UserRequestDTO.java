package com.dayu.yiyibushe.web.dto;

import com.dayu.yiyibushe.common.BaseRequest;

/**
 * 说明：用户查询请求 DTO，对外接收前端参数，继承 BaseRequest 获得分页能力。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class UserRequestDTO extends BaseRequest {

    private static final long serialVersionUID = 6102938475610293847L;

    /** 用户 ID（精确查询） */
    private Long userId;

    /** 用户名（模糊查询） */
    private String username;

    /** 手机号 */
    private String phone;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /**
     * 无参构造器
     */
    public UserRequestDTO() {
    }

    /**
     * Getter method for property <tt>userId</tt>.
     *
     * @return property value of userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Setter method for property <tt>userId</tt>.
     *
     * @param userId
     *     value to be assigned to property userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Getter method for property <tt>username</tt>.
     *
     * @return property value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter method for property <tt>username</tt>.
     *
     * @param username
     *     value to be assigned to property username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter method for property <tt>phone</tt>.
     *
     * @return property value of phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Setter method for property <tt>phone</tt>.
     *
     * @param phone
     *     value to be assigned to property phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Getter method for property <tt>status</tt>.
     *
     * @return property value of status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     *
     * @param status
     *     value to be assigned to property status
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
