package com.yiyibushe.common.exception;

/**
 * 业务错误码枚举。
 * <p>
 * 所有对外抛出的业务异常必须指定一个 ErrorCode，便于前端和日志统一识别错误类型。
 *
 * @author Witty·Kid Fisher
 */
public enum ErrorCode {

    SUCCESS(0, "成功"),

    // ---------- 参数类 1xxx ----------
    PARAM_INVALID(1001, "参数不合法"),
    PARAM_NULL(1002, "参数不能为空"),

    // ---------- OSS 类 2xxx ----------
    OSS_UPLOAD_FAILED(2001, "OSS 上传失败"),
    OSS_DOWNLOAD_FAILED(2002, "OSS 下载失败"),

    // ---------- 大模型类 3xxx ----------
    DASHSCOPE_TASK_FAILED(3001, "大模型任务执行失败"),
    DASHSCOPE_TASK_TIMEOUT(3002, "大模型任务超时"),
    DASHSCOPE_RESPONSE_INVALID(3003, "大模型返回数据异常"),

    // ---------- 系统类 9xxx ----------
    UNKNOWN_ERROR(9999, "系统未知错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
