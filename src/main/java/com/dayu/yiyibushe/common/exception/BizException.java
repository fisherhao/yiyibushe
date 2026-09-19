package com.dayu.yiyibushe.common.exception;

/**
 * 说明：统一业务异常，携带 errCode 与 errMsg。
 * <p>
 * 业务代码禁止直接 {@code throw new RuntimeException("xxx")}，
 * 必须通过 {@link BizException} 抛出，并指定 {@link ErrorCode}。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 5678901234567890123L;

    /** 错误码 */
    private final String errCode;

    /** 错误信息 */
    private final String errMsg;

    /**
     * 通过错误码枚举构造异常
     *
     * @param errorCode
     *     错误码枚举
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errCode = errorCode.getCode();
        this.errMsg = errorCode.getMsg();
    }

    /**
     * 通过错误码枚举与自定义信息构造异常
     *
     * @param errorCode
     *     错误码枚举
     * @param message
     *     自定义错误信息
     */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errCode = errorCode.getCode();
        this.errMsg = message;
    }

    /**
     * 直接通过错误码与错误信息构造异常
     *
     * @param errCode
     *     错误码
     * @param errMsg
     *     错误信息
     */
    public BizException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    /**
     * 通过错误码枚举与原始异常构造异常
     *
     * @param errorCode
     *     错误码枚举
     * @param cause
     *     原始异常
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.errCode = errorCode.getCode();
        this.errMsg = errorCode.getMsg();
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
     * Getter method for property <tt>errMsg</tt>.
     *
     * @return property value of errMsg
     */
    public String getErrMsg() {
        return errMsg;
    }
}
