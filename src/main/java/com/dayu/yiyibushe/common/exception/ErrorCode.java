package com.dayu.yiyibushe.common.exception;

/**
 * 说明：统一错误码接口，所有错误码枚举必须实现本接口。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return 错误码字符串
     */
    String getCode();

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    String getMsg();
}
