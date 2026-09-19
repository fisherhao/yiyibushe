package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 说明：用户表 users 的 MyBatis XML Mapper（SQL 全部写在 resources/mapper/UserMybatisMapper.xml）。
 * <p>
 * 遵循全库统一规范：主键 id 无语义、由发号器生成（插入前外部赋值）；
 * 业务用户 ID 为 user_id，查询/更新/删除一律按 user_id 进行；
 * gmt_create 由数据库默认填充、gmt_modify 更新时由数据库自动刷新，因此 insert 语句不含时间列。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Mapper
public interface UserMybatisMapper {

    /**
     * 按业务用户 ID 查询用户
     *
     * @param userId
     *               业务用户 ID
     * @return 用户 PO，不存在返回 null
     */
    UserPO selectByUserId(@Param("userId") Long userId);

    /**
     * 按用户名查询用户（登录、注册判重使用）
     *
     * @param username
     *                 用户名
     * @return 用户 PO，不存在返回 null
     */
    UserPO selectByUsername(@Param("username") String username);

    /**
     * 查询全部用户（按业务用户 ID 升序）
     *
     * @return 用户 PO 列表
     */
    List<UserPO> selectAll();

    /**
     * 新增用户（主键 id 与业务 user_id 均需在调用前由发号器赋值，时间列由数据库默认填充）
     *
     * @param userPO
     *               用户 PO
     * @return 受影响行数
     */
    int insert(UserPO userPO);

    /**
     * 按业务用户 ID 更新用户（gmt_modify 由数据库自动刷新）
     *
     * @param userPO
     *               用户 PO
     * @return 受影响行数
     */
    int updateByUserId(UserPO userPO);

    /**
     * 按业务用户 ID 删除用户
     *
     * @param userId
     *               业务用户 ID
     * @return 受影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
}
