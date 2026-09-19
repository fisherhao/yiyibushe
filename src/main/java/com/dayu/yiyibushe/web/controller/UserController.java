package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.app.service.UserService;
import com.dayu.yiyibushe.common.ApiResult;
import com.dayu.yiyibushe.common.ExecuteTemplate;
import com.dayu.yiyibushe.domain.model.UserModel;
import com.dayu.yiyibushe.web.dto.UserRequestDTO;
import com.dayu.yiyibushe.web.dto.UserResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 说明：用户 HTTP 接口，统一通过 {@link ExecuteTemplate#send} 调用业务逻辑
 * （send 内含 before 参数校验、doExecute 业务执行两段），无需在每个方法里重复写
 * try-catch 和参数判空。
 * <p>
 * 安全约定：用户 ID 不出现在 URL 路径中（避免日志、浏览记录、Referer 泄露），
 * 统一由 POST 请求体携带。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /** 用户业务接口 */
    private final UserService userService;

    /**
     * 构造器注入 UserService
     *
     * @param userService
     *                    用户业务接口
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页查询用户列表
     * <p>
     * 演示 ExecuteTemplate 的 lambda 用法：把请求透传给 Service，
     * 模板自动完成参数校验、异常捕获、分页信息与 traceId 回填。
     *
     * @param request
     *                用户查询请求
     * @return 统一返回体，data 为用户响应列表
     */
    @PostMapping("/list")
    public ApiResult<List<UserResponseDTO>> listUsers(@RequestBody UserRequestDTO request) {
        return ExecuteTemplate.send(request, req -> userService.queryUsers(req));
    }

    /**
     * 根据用户 ID 查询单个用户。
     * <p>
     * userId 放在 POST 请求体中而非路径变量，避免在 URL 里暴露用户标识。
     *
     * @param request
     *                用户查询请求（userId 在请求体中）
     * @return 统一返回体，data 为用户响应
     */
    @PostMapping("/detail")
    public ApiResult<UserResponseDTO> getUser(@RequestBody UserRequestDTO request) {
        return ExecuteTemplate.send(request, req -> {
            // 此处可以写更复杂的业务编排，演示 lambda 内部多步处理
            UserModel model = userService.getUserById(req.getUserId());
            UserResponseDTO dto = new UserResponseDTO();
            dto.setUserId(model.getUserId());
            dto.setUsername(model.getUsername());
            dto.setNickname(model.getNickname());
            dto.setStatus(model.getStatus());
            dto.setRegisterTime(model.getRegisterTime());
            return dto;
        });
    }
}
