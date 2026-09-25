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
     * 获取用户 ID（精确查询）
     *
     * @return 用户 ID（精确查询）
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户 ID（精确查询）
     *
     * @param userId
                     用户 ID（精确查询）
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名（模糊查询）
     *
     * @return 用户名（模糊查询）
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名（模糊查询）
     *
     * @param username
                       用户名（模糊查询）
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取手机号
     *
     * @return 手机号
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号
     *
     * @param phone
                    手机号
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status
                     状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
