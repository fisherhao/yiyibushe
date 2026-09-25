package com.dayu.yiyibushe.app.service.impl;

import com.dayu.yiyibushe.app.service.AuthService;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.util.PasswordUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.dao.mapper.UserMapper;
import com.dayu.yiyibushe.dao.po.UserPO;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 认证服务实现：基于用户存储完成注册、登录、修改密码与找回密码。
 * <p>
 * 按需求不做账号验证、密码强度校验，仅保证用户名非空且不重复，
 * 密码通过 PasswordUtilExt 以 PBKDF2 加盐保存，不存明文；
 * 历史 SHA-256 密码在登录成功后自动升级为 PBKDF2。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
@Service
public class AuthServiceImpl implements AuthService {

    /** 用户正常状态 */
    private static final int USER_STATUS_ACTIVE = 1;

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册：用户名非空且不重复，密码 PBKDF2 加盐后落库
     *
     * @param username
     *     用户名
     * @param rawPassword
     *     明文密码
     * @param nickname
     *     昵称，可空（空则取用户名）
     * @return 登录主体
     */
    @Override
    public LoginPrincipal register(String username, String rawPassword, String nickname) {
        if (StringUtilExt.isBlank(username) || StringUtilExt.isBlank(rawPassword)) {
            throw new BizException(ParamErrorCode.USERNAME_PASSWORD_BLANK);
        }
        if (Objects.nonNull(userMapper.selectByUsername(username))) {
            throw new BizException(BizErrorCode.USERNAME_EXISTS);
        }
        UserPO userPO = new UserPO();
        userPO.setUsername(username.trim());
        userPO.setPassword(PasswordUtilExt.encode(rawPassword));
        userPO.setNickname(StringUtilExt.isBlank(nickname) ? username.trim() : nickname.trim());
        userPO.setStatus(USER_STATUS_ACTIVE);
        // gmt_create / gmt_modify 由数据库默认值填充，这里不传时间
        userMapper.insert(userPO);
        return toPrincipal(userPO);
    }

    /**
     * 登录：校验用户存在、状态正常、密码匹配；旧 SHA-256 密码登录后自动升级
     *
     * @param username
     *     用户名
     * @param rawPassword
     *     明文密码
     * @return 登录主体
     */
    @Override
    public LoginPrincipal login(String username, String rawPassword) {
        UserPO userPO = userMapper.selectByUsername(username);
        if (Objects.isNull(userPO)
                || !Objects.equals(userPO.getStatus(), USER_STATUS_ACTIVE)
                || !PasswordUtilExt.matches(rawPassword, userPO.getPassword())) {
            throw new BizException(BizErrorCode.LOGIN_FAILED);
        }
        // 旧版固定盐 SHA-256 密码在登录成功后平滑升级为 PBKDF2
        if (!StringUtilExt.startsWith(userPO.getPassword(), PasswordUtilExt.PBKDF2_PREFIX)) {
            userPO.setPassword(PasswordUtilExt.encode(rawPassword));
            userMapper.updateByUserId(userPO);
        }
        return toPrincipal(userPO);
    }

    /**
     * 修改密码：校验原密码后更新为新密码，调用方负责踢掉该用户的历史会话
     *
     * @param userId
     *     用户 ID
     * @param oldRawPassword
     *     原密码
     * @param newRawPassword
     *     新密码
     */
    @Override
    public void changePassword(Long userId, String oldRawPassword, String newRawPassword) {
        if (Objects.isNull(userId)) {
            throw new BizException(ParamErrorCode.USER_ID_BLANK);
        }
        if (StringUtilExt.isBlank(newRawPassword)) {
            throw new BizException(BizErrorCode.NEW_PASSWORD_BLANK);
        }
        UserPO userPO = userMapper.selectByUserId(userId);
        if (Objects.isNull(userPO)) {
            throw new BizException(BizErrorCode.USER_NOT_FOUND);
        }
        if (!PasswordUtilExt.matches(oldRawPassword, userPO.getPassword())) {
            throw new BizException(BizErrorCode.OLD_PASSWORD_WRONG);
        }
        userPO.setPassword(PasswordUtilExt.encode(newRawPassword));
        userMapper.updateByUserId(userPO);
    }

    /**
     * 找回密码：暂无邮箱/短信通道，直接按用户名重置，调用方负责踢掉历史会话。
     * 生产环境必须先完成邮箱或短信身份核验。
     *
     * @param username
     *     用户名
     * @param newRawPassword
     *     新密码
     * @return 被重置用户的 ID
     */
    @Override
    public Long resetPassword(String username, String newRawPassword) {
        if (StringUtilExt.isBlank(username) || StringUtilExt.isBlank(newRawPassword)) {
            throw new BizException(ParamErrorCode.USERNAME_PASSWORD_BLANK);
        }
        UserPO userPO = userMapper.selectByUsername(username);
        if (Objects.isNull(userPO)) {
            throw new BizException(BizErrorCode.USER_NOT_FOUND);
        }
        userPO.setPassword(PasswordUtilExt.encode(newRawPassword));
        userMapper.updateByUserId(userPO);
        return userPO.getUserId();
    }

    /**
     * PO 转登录主体
     *
     * @param userPO
     *     用户 PO
     * @return 登录主体
     */
    private LoginPrincipal toPrincipal(UserPO userPO) {
        return new LoginPrincipal(userPO.getUserId(), userPO.getUsername(), userPO.getNickname());
    }
}
