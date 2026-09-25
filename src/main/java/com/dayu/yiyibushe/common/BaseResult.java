package com.dayu.yiyibushe.common;

import java.io.Serializable;

/**
 * 说明：统一响应基类，所有出参 DTO 继承本类，携带链路追踪 ID 与分页信息。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class BaseResult implements Serializable {

    private static final long serialVersionUID = 9123456789012345678L;

    /** 链路追踪 ID */
    private String traceId;

    /** 当前页码 */
    private Integer pageIndex;

    /** 每页条数 */
    private Integer pageSize;

    /**
     * 无参构造器
     */
    public BaseResult() {
    }

    /**
     * 获取链路追踪 ID
     *
     * @return 链路追踪 ID
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置链路追踪 ID
     *
     * @param traceId
                      链路追踪 ID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 获取当前页码
     *
     * @return 当前页码
     */
    public Integer getPageIndex() {
        return pageIndex;
    }

    /**
     * 设置当前页码
     *
     * @param pageIndex
                        当前页码
     */
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * 获取每页条数
     *
     * @return 每页条数
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数
     *
     * @param pageSize
                       每页条数
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
