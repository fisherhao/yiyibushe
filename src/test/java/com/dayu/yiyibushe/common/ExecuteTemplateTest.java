package com.dayu.yiyibushe.common;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.exception.SystemErrorCode;
import com.dayu.yiyibushe.web.dto.UserRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 说明：{@link ExecuteTemplate} 单元测试，覆盖 before 参数校验、doExecute 成功/异常等分支。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
class ExecuteTemplateTest {

    /**
     * request 为 null 时 before 校验失败，返回参数错误
     */
    @Test
    void send_whenRequestIsNull_returnsRequestNullError() {
        ApiResult<String> result = ExecuteTemplate.send(null, req -> "ok");
        assertFalse(result.isSuccess());
        assertEquals(ParamErrorCode.REQUEST_NULL.getCode(), result.getErrCode());
        assertNull(result.getData());
    }

    /**
     * execute 函数为 null 时返回参数错误
     */
    @Test
    void send_whenActionIsNull_returnsActionNullError() {
        UserRequestDTO request = new UserRequestDTO();
        ApiResult<String> result = ExecuteTemplate.send(request, (Function<UserRequestDTO, String>) null);
        assertFalse(result.isSuccess());
        assertEquals(ParamErrorCode.ACTION_NULL.getCode(), result.getErrCode());
    }

    /**
     * 正常执行业务逻辑时返回成功，并回填分页信息
     */
    @Test
    void send_whenSuccess_returnsDataAndPaging() {
        UserRequestDTO request = new UserRequestDTO();
        request.setPageIndex(2);
        request.setPageSize(20);

        ApiResult<String> result = ExecuteTemplate.send(request, req -> "hello");

        assertTrue(result.isSuccess());
        assertEquals("hello", result.getData());
        assertEquals(2, result.getPageIndex());
        assertEquals(20, result.getPageSize());
    }

    /**
     * doExecute 抛出 BizException 时返回对应错误码
     */
    @Test
    void send_whenBizException_returnsBizError() {
        UserRequestDTO request = new UserRequestDTO();
        ApiResult<String> result = ExecuteTemplate.send(request,
                req -> {
                    throw new BizException(BizErrorCode.USER_NOT_FOUND);
                });

        assertFalse(result.isSuccess());
        assertEquals(BizErrorCode.USER_NOT_FOUND.getCode(), result.getErrCode());
        assertEquals(BizErrorCode.USER_NOT_FOUND.getMsg(), result.getErrMsg());
    }

    /**
     * doExecute 抛出未知异常时兜底返回系统错误
     */
    @Test
    void send_whenUnknownException_returnsSystemError() {
        UserRequestDTO request = new UserRequestDTO();
        ApiResult<String> result = ExecuteTemplate.send(request,
                req -> {
                    throw new IllegalStateException("boom");
                });

        assertFalse(result.isSuccess());
        assertEquals(SystemErrorCode.SYSTEM_ERROR.getCode(), result.getErrCode());
        assertEquals(SystemErrorCode.SYSTEM_ERROR.getMsg(), result.getErrMsg());
    }

    /**
     * 使用完整回调时：自定义 before 校验失败则不执行 doExecute
     */
    @Test
    void send_whenCustomBeforeRejected_skipsExecute() {
        UserRequestDTO request = new UserRequestDTO();
        boolean[] executed = {false};
        ExecuteTemplate.SendCallback<UserRequestDTO, String> callback = new ExecuteTemplate.SendCallback<>() {
            /**
             * 先执行默认校验，再主动抛参数异常模拟校验失败
             *
             * @param callbackRequest
             *     请求对象
             */
            @Override
            protected void before(UserRequestDTO callbackRequest) {
                super.before(callbackRequest);
                throw new BizException(ParamErrorCode.PARAM_INVALID);
            }

            /**
             * 业务执行（若被调用则标记 executed）
             *
             * @param callbackRequest
             *     请求对象
             * @return 固定返回 ok
             */
            @Override
            protected String doExecute(UserRequestDTO callbackRequest) {
                executed[0] = true;
                return "ok";
            }
        };

        ApiResult<String> result = ExecuteTemplate.send(request, callback);

        assertFalse(result.isSuccess());
        assertEquals(ParamErrorCode.PARAM_INVALID.getCode(), result.getErrCode());
        assertFalse(executed[0]);
    }
}
