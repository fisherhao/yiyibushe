package com.dayu.yiyibushe.common.exception;

/**
 * 说明：系统级错误码枚举（1xxx 段）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public enum SystemErrorCode implements ErrorCode {

    /** 系统未知错误 */
    SYSTEM_ERROR("1001", "系统未知错误"),
    /** 系统繁忙 */
    SYSTEM_BUSY("1002", "系统繁忙，请稍后再试"),
    /** 数据库操作失败 */
    DB_ERROR("1003", "数据库操作失败"),
    /** 远程调用失败 */
    RPC_ERROR("1004", "远程调用失败");

    private final String code;
    private final String msg;

    SystemErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>msg</tt>.
     *
     * @return property value of msg
     */
    @Override
    public String getMsg() {
        return msg;
    }
}
