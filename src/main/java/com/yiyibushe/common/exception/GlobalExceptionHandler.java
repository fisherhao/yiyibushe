package com.yiyibushe.common.exception;

import com.yiyibushe.common.ApiResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：把异常统一包装成 {@link ApiResult} 返回给前端。
 *
 * @author Witty·Kid Fisher
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：直接返回错误码与消息。
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Object> handleBizException(BizException e) {
        return ApiResult.fail(e.getCode(), e.getMessage());
    }

    /**
     * 兜底异常：返回未知错误码。
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleException(Exception e) {
        return ApiResult.fail(ErrorCode.UNKNOWN_ERROR.getCode(), e.getMessage());
    }
}
