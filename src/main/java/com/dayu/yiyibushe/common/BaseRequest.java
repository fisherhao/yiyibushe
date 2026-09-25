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
     * 获取当前页码，默认 1
     *
     * @return 当前页码，默认 1
     */
    public Integer getPageIndex() {
        return pageIndex;
    }

    /**
     * 设置当前页码，默认 1
     *
     * @param pageIndex
                        当前页码，默认 1
     */
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * 获取每页条数，默认 10
     *
     * @return 每页条数，默认 10
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数，默认 10
     *
     * @param pageSize
                       每页条数，默认 10
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
