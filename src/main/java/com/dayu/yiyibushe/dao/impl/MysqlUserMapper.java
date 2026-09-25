package com.dayu.yiyibushe.dao.impl;

import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.exception.ParamErrorCode;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.dao.mapper.UserMapper;
import com.dayu.yiyibushe.dao.mybatis.UserMybatisMapper;
import com.dayu.yiyibushe.dao.po.UserPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 说明：用户 Mapper 的 MySQL 实现（MyBatis + HikariCP 连接池），UserMapper 的唯一实现。
 * <p>
 * SQL 全部写在 {@code resources/mapper/UserMybatisMapper.xml}，本类只做两件事：
 * 注入 {@link IdUtil} 为新增用户分配 18 位业务 user_id（主键 id 由数据库自增）；
 * 委托 MyBatis Mapper 完成读写，查询与更新按业务 user_id 进行。
 * <p>
 * 用户数据统一落 MySQL，本地 JSON 文件实现（原 LocalFileUserMapper）已移除。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.6
 */
@Repository
public class MysqlUserMapper implements UserMapper {

    /** 用户表的 MyBatis Mapper（XML 形式） */
    @Autowired
    private UserMybatisMapper userMybatisMapper;

    /**
     * 按业务用户 ID 查询用户
     *
     * @param userId
     *     业务用户 ID
     * @return 用户 PO，不存在返回 null
     */
    @Override
    public UserPO selectByUserId(Long userId) {
        return userMybatisMapper.selectByUserId(userId);
    }

    /**
     * 按用户名查询用户
     *
     * @param username
     *     用户名
     * @return 用户 PO，不存在返回 null
     */
    @Override
    public UserPO selectByUsername(String username) {
        if (Objects.isNull(username)) {
            return null;
        }
        return userMybatisMapper.selectByUsername(username);
    }

    /**
     * 查询全部用户（按业务用户 ID 升序）
     *
     * @return 用户 PO 列表
     */
    @Override
    public List<UserPO> selectAll() {
        return userMybatisMapper.selectAll();
    }

    /**
     * 新增用户：先用发号器分配主键 id 与业务 user_id（各自独立发号），再委托 MyBatis 落库。
     * gmt_create 由数据库默认填充，代码不传。
     *
     * @param userPO
     *     用户 PO
     * @return 受影响行数
     */
    @Override
    public int insert(UserPO userPO) {
        if (Objects.isNull(userPO)) {
            throw new BizException(ParamErrorCode.USER_INFO_BLANK);
        }
        userPO.setUserId(IdUtil.nextId());
        return userMybatisMapper.insert(userPO);
    }

    /**
     * 按业务用户 ID 更新用户（gmt_modify 由数据库 ON UPDATE 自动刷新）
     *
     * @param userPO
     *     用户 PO
     * @return 受影响行数
     */
    @Override
    public int updateByUserId(UserPO userPO) {
        if (Objects.isNull(userPO)) {
            throw new BizException(ParamErrorCode.USER_INFO_BLANK);
        }
        return userMybatisMapper.updateByUserId(userPO);
    }

    /**
     * 按业务用户 ID 删除用户
     *
     * @param userId
     *     业务用户 ID
     * @return 受影响行数
     */
    @Override
    public int deleteByUserId(Long userId) {
        return userMybatisMapper.deleteByUserId(userId);
    }
}
