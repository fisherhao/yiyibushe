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
     * 成功返回
     *
     * @param data
     *     业务数据
     * @param <T>
     *     数据类型
     * @return ApiResult
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.success = true;
        result.data = data;
        return result;
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
        ApiResult<T> result = new ApiResult<>();
        result.success = false;
        result.errCode = errCode;
        result.errMsg = errMsg;
        return result;
    }

    /**
     * 是否成功
     *
     * @return true 表示成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置是否成功
     *
     * @param success
                      是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取业务数据
     *
     * @return 业务数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置业务数据
     *
     * @param data
                   业务数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrCode() {
        return errCode;
    }

    /**
     * 设置错误码
     *
     * @param errCode
                      错误码
     */
    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrMsg() {
        return errMsg;
    }

    /**
     * 设置错误信息
     *
     * @param errMsg
                     错误信息
     */
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
