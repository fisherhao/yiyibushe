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
     * Getter method for property <tt>traceId</tt>.
     *
     * @return property value of traceId
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * Setter method for property <tt>traceId</tt>.
     *
     * @param traceId
     *     value to be assigned to property traceId
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * Getter method for property <tt>pageIndex</tt>.
     *
     * @return property value of pageIndex
     */
    public Integer getPageIndex() {
        return pageIndex;
    }

    /**
     * Setter method for property <tt>pageIndex</tt>.
     *
     * @param pageIndex
     *     value to be assigned to property pageIndex
     */
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * Getter method for property <tt>pageSize</tt>.
     *
     * @return property value of pageSize
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Setter method for property <tt>pageSize</tt>.
     *
     * @param pageSize
     *     value to be assigned to property pageSize
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
