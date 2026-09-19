package com.dayu.yiyibushe.common;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.exception.SystemErrorCode;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * 说明：统一执行模板，封装「前置校验 → 业务执行」的标准流程。
 * <p>
 * Controller 层统一通过 {@code send} 调用业务逻辑：
 * <ol>
 * <li>{@code before}：前置参数/类型校验，默认实现校验请求对象非空；</li>
 * <li>{@code doExecute}：业务执行，返回业务数据，由模板统一包装成 {@link ApiResult}。</li>
 * </ol>
 * 业务方通常只需提供 doExecute，需要额外校验时重写 before，
 * 无需在每个接口里重复写 try-catch。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
public final class ExecuteTemplate {

    /**
     * 私有构造器：工具类不允许实例化
     */
    private ExecuteTemplate() {
    }

    /**
     * 便捷发送：使用默认 before（请求非空校验）
     *
     * @param request
     *                请求对象（继承 BaseRequest）
     * @param execute
     *                业务执行函数，接收 request 返回业务数据
     * @param <R>
     *                请求类型
     * @param <T>
     *                响应数据类型
     * @return 统一返回体 ApiResult
     */
    public static <R extends BaseRequest, T> ApiResult<T> send(R request, Function<R, T> execute) {
        if (Objects.isNull(execute)) {
            return ApiResult.fail(ParamErrorCode.ACTION_NULL.getCode(), ParamErrorCode.ACTION_NULL.getMsg());
        }
        return send(request, new SendCallback<>() {
            /**
             * 委托给外部传入的业务函数执行
             *
             * @param callbackRequest
             *                        请求对象
             * @return 业务数据
             */
            @Override
            protected T doExecute(R callbackRequest) {
                return execute.apply(callbackRequest);
            }
        });
    }

    /**
     * 模板发送：依次执行 before → doExecute，并统一捕获异常、回填分页与 traceId
     *
     * @param request
     *                 请求对象（继承 BaseRequest）
     * @param callback
     *                 发送回调（提供 doExecute，before 可选重写）
     * @param <R>
     *                 请求类型
     * @param <T>
     *                 响应数据类型
     * @return 统一返回体 ApiResult
     */
    public static <R extends BaseRequest, T> ApiResult<T> send(R request, SendCallback<R, T> callback) {
        if (Objects.isNull(callback)) {
            return ApiResult.fail(ParamErrorCode.ACTION_NULL.getCode(), ParamErrorCode.ACTION_NULL.getMsg());
        }
        ApiResult<T> result = new ApiResult<>();
        result.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        try {
            // 1. 前置校验（默认校验 request 非空，业务方可重写补充类型/参数校验）
            callback.before(request);
            // 校验通过后回填分页信息
            result.setPageIndex(request.getPageIndex());
            result.setPageSize(request.getPageSize());
            // 2. 业务执行，结果统一包装
            T data = callback.doExecute(request);
            result.setSuccess(true);
            result.setData(data);
        } catch (BizException e) {
            result.setSuccess(false);
            result.setErrCode(e.getErrCode());
            result.setErrMsg(e.getErrMsg());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrCode(SystemErrorCode.SYSTEM_ERROR.getCode());
            result.setErrMsg(SystemErrorCode.SYSTEM_ERROR.getMsg());
        }
        return result;
    }

    /**
     * 发送回调：定义模板执行的前后两段。
     * <p>
     * before 有默认实现，业务方通常只需实现 {@link #doExecute}；
     * 需要额外参数校验时重写 {@link #before}。
     *
     * @param <R>
     *            请求类型
     * @param <T>
     *            响应数据类型
     */
    public abstract static class SendCallback<R extends BaseRequest, T> {

        /**
         * 前置参数/类型校验，默认校验请求对象非空
         *
         * @param request
         *                请求对象，可能为 null
         */
        protected void before(R request) {
            if (Objects.isNull(request)) {
                throw new BizException(ParamErrorCode.REQUEST_NULL);
            }
        }

        /**
         * 业务执行，返回业务数据（模板负责包装为 ApiResult）
         *
         * @param request
         *                请求对象
         * @return 业务数据
         */
        protected abstract T doExecute(R request);
    }
}
