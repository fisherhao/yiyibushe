package com.dayu.yiyibushe.app.service.impl;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.web.dto.UserRequestDTO;
import com.dayu.yiyibushe.web.dto.UserResponseDTO;
import com.dayu.yiyibushe.dao.po.UserPO;
import com.dayu.yiyibushe.dao.mapper.UserMapper;
import com.dayu.yiyibushe.domain.model.UserModel;
import com.dayu.yiyibushe.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 说明：用户业务实现类，编排 UserMapper 完成数据访问，并在内部做 Entity/Model/DTO 转换。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据请求条件查询用户列表
     *
     * @param request
     *     用户查询请求
     * @return 用户响应列表
     */
    @Override
    public List<UserResponseDTO> queryUsers(UserRequestDTO request) {
        // 演示：真实场景应按 request 条件构建查询，这里简化为查全部
        return CollectionUtilExt.mapToList(userMapper.selectAll(), this::toResponseDTO);
    }

    /**
     * 根据用户 ID 查询单个用户
     *
     * @param userId
     *     用户 ID
     * @return 用户业务模型，不存在返回 null
     */
    @Override
    public UserModel getUserById(Long userId) {
        if (Objects.isNull(userId)) {
            throw new BizException(ParamErrorCode.USER_ID_BLANK);
        }
        UserPO entity = userMapper.selectByUserId(userId);
        if (Objects.isNull(entity)) {
            throw new BizException(BizErrorCode.USER_NOT_FOUND);
        }
        return toModel(entity);
    }

    /**
     * 新增用户
     *
     * @param userModel
     *     用户业务模型
     * @return 新增后的用户 ID
     */
    @Override
    public Long createUser(UserModel userModel) {
        if (Objects.isNull(userModel)) {
            throw new BizException(ParamErrorCode.USER_INFO_BLANK);
        }
        UserPO entity = toEntity(userModel);
        userMapper.insert(entity);
        return entity.getUserId();
    }

    /**
     * Entity 转 ResponseDTO
     *
     * @param entity
     *     用户实体
     * @return 用户响应 DTO
     */
    private UserResponseDTO toResponseDTO(UserPO entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setNickname(entity.getNickname());
        dto.setStatus(entity.getStatus());
        dto.setRegisterTime(entity.getGmtCreate());
        return dto;
    }

    /**
     * Entity 转 Model
     *
     * @param entity
     *     用户实体
     * @return 用户业务模型
     */
    private UserModel toModel(UserPO entity) {
        UserModel model = new UserModel();
        model.setUserId(entity.getUserId());
        model.setUsername(entity.getUsername());
        model.setNickname(entity.getNickname());
        model.setStatus(entity.getStatus());
        model.setRegisterTime(entity.getGmtCreate());
        return model;
    }

    /**
     * Model 转 Entity
     *
     * @param model
     *     用户业务模型
     * @return 用户实体
     */
    private UserPO toEntity(UserModel model) {
        UserPO entity = new UserPO();
        entity.setUserId(model.getUserId());
        entity.setUsername(model.getUsername());
        entity.setNickname(model.getNickname());
        entity.setStatus(model.getStatus());
        return entity;
    }
}
