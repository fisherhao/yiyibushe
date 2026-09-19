package com.dayu.yiyibushe.common.exception;

import com.dayu.yiyibushe.common.ApiResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 说明：全局异常处理器，把异常统一包装成 {@link ApiResult} 返回给前端。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     *
     * @param e
     *     业务异常
     * @return 统一返回体
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Object> handleBizException(BizException e) {
        return ApiResult.fail(e.getErrCode(), e.getErrMsg());
    }

    /**
     * 上传文件超限处理
     *
     * @param e
     *     文件大小超限异常
     * @return 统一返回体
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ApiResult.fail(ParamErrorCode.FILE_TOO_LARGE.getCode(), ParamErrorCode.FILE_TOO_LARGE.getMsg());
    }

    /**
     * 兜底异常处理
     *
     * @param e
     *     未知异常
     * @return 统一返回体
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleException(Exception e) {
        return ApiResult.fail(SystemErrorCode.SYSTEM_ERROR.getCode(), SystemErrorCode.SYSTEM_ERROR.getMsg());
    }
}
