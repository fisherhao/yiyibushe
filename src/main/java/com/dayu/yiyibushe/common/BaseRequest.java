package com.dayu.yiyibushe.common;

import java.io.Serializable;

/**
 * 说明：统一请求基类，所有入参 DTO 继承本类，提供分页默认参数。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class BaseRequest implements Serializable {

    private static final long serialVersionUID = 7823456789012345678L;

    /** 当前页码，默认 1 */
    private Integer pageIndex = 1;

    /** 每页条数，默认 10 */
    private Integer pageSize = 10;

    /**
     * 无参构造器
     */
    public BaseRequest() {
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
