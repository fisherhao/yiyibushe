package com.dayu.yiyibushe.app.service;

import com.dayu.yiyibushe.web.dto.UserRequestDTO;
import com.dayu.yiyibushe.web.dto.UserResponseDTO;
import com.dayu.yiyibushe.domain.model.UserModel;

import java.util.List;

/**
 * 说明：用户业务接口，定义用户相关的业务操作。
 * <p>
 * Controller 与其他 Service 只依赖本接口，不直接依赖实现类。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface UserService {

    /**
     * 根据请求条件查询用户列表
     *
     * @param request
     *     用户查询请求
     * @return 用户响应列表
     */
    List<UserResponseDTO> queryUsers(UserRequestDTO request);

    /**
     * 根据用户 ID 查询单个用户
     *
     * @param userId
     *     用户 ID
     * @return 用户业务模型，不存在返回 null
     */
    UserModel getUserById(Long userId);

    /**
     * 新增用户
     *
     * @param userModel
     *     用户业务模型
     * @return 新增后的用户 ID
     */
    Long createUser(UserModel userModel);
}
