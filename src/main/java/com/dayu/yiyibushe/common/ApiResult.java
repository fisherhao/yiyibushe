package com.dayu.yiyibushe.common;

import java.io.Serializable;

/**
 * 说明：统一接口返回体，继承 BaseResult 并实现 Serializable。
 * <p>
 * 不依赖任何第三方 JSON 注解，序列化后结构为：
 * <pre>
 * {
 *   "traceId": "...",
 *   "pageIndex": 1,
 *   "pageSize": 10,
 *   "success": true,
 *   "data": {...},
 *   "errCode": null,
 *   "errMsg": null
 * }
 * </pre>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ApiResult<T> extends BaseResult implements Serializable {

    private static final long serialVersionUID = 3456789012345678901L;

    /** 是否成功 */
    private boolean success;

    /** 业务数据 */
    private T data;

    /** 错误码 */
    private String errCode;

    /** 错误信息 */
    private String errMsg;

    /**
     * 无参构造器
     */
    public ApiResult() {
    }

    /**
     * 构造成功返回
     *
     * @param data
     *     业务数据
     */
    private ApiResult(T data) {
        this.success = true;
        this.data = data;
    }

    /**
     * 构造失败返回
     *
     * @param errCode
     *     错误码
     * @param errMsg
     *     错误信息
     */
    private ApiResult(String errCode, String errMsg) {
        this.success = false;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    /**
     * 成功返回
     *
     * @param data
     *     业务数据
     * @param <T>
     *     数据类型
     * @return ApiResult
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(data);
    }

    /**
     * 失败返回
     *
     * @param errCode
     *     错误码
     * @param errMsg
     *     错误信息
     * @param <T>
     *     数据类型
     * @return ApiResult
     */
    public static <T> ApiResult<T> fail(String errCode, String errMsg) {
        return new ApiResult<>(errCode, errMsg);
    }

    /**
     * Getter method for property <tt>success</tt>.
     *
     * @return property value of success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     *
     * @param success
     *     value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Getter method for property <tt>data</tt>.
     *
     * @return property value of data
     */
    public T getData() {
        return data;
    }

    /**
     * Setter method for property <tt>data</tt>.
     *
     * @param data
     *     value to be assigned to property data
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Getter method for property <tt>errCode</tt>.
     *
     * @return property value of errCode
     */
    public String getErrCode() {
        return errCode;
    }

    /**
     * Setter method for property <tt>errCode</tt>.
     *
     * @param errCode
     *     value to be assigned to property errCode
     */
    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    /**
     * Getter method for property <tt>errMsg</tt>.
     *
     * @return property value of errMsg
     */
    public String getErrMsg() {
        return errMsg;
    }

    /**
     * Setter method for property <tt>errMsg</tt>.
     *
     * @param errMsg
     *     value to be assigned to property errMsg
     */
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
