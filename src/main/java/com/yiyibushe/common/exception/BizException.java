package com.yiyibushe.common.exception;

/**
 * 统一业务异常。
 * <p>
 * 业务代码禁止直接 {@code throw new RuntimeException("xxx")}，
 * 必须通过 {@link BizException} 抛出，并指定 {@link ErrorCode}。
 *
 * @author Witty·Kid Fisher
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
