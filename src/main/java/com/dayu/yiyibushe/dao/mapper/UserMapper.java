package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.dao.po.UserPO;

import java.util.List;

/**
 * 说明：用户数据访问接口，定义对用户存储的 CRUD 操作。
 * <p>
 * 查询与更新一律按「业务用户 ID」（user_id）进行，主键 id 无语义、不参与业务条件。
 * <p>
 * 存储实现为 {@code MysqlUserMapper}（MyBatis + MySQL）；如更换存储介质，
 * 只需新增实现并替换 Bean，上层代码不变。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
public interface UserMapper {

    /**
     * 根据业务用户 ID 查询用户
     *
     * @param userId
     *               业务用户 ID
     * @return 用户 PO，不存在返回 null
     */
    UserPO selectByUserId(Long userId);

    /**
     * 根据用户名查询用户（登录、注册判重使用）
     *
     * @param username
     *                 用户名
     * @return 用户 PO，不存在返回 null
     */
    UserPO selectByUsername(String username);

    /**
     * 查询全部用户
     *
     * @return 用户 PO 列表
     */
    List<UserPO> selectAll();

    /**
     * 新增用户
     *
     * @param userPO
     *               用户 PO
     * @return 影响行数
     */
    int insert(UserPO userPO);

    /**
     * 根据业务用户 ID 更新用户
     *
     * @param userPO
     *               用户 PO
     * @return 影响行数
     */
    int updateByUserId(UserPO userPO);

    /**
     * 根据业务用户 ID 删除用户
     *
     * @param userId
     *               业务用户 ID
     * @return 影响行数
     */
    int deleteByUserId(Long userId);
}
