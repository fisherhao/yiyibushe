package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.CredentialPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 凭证表 ai_credential 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface CredentialMybatisMapper {

    /**
     * 按厂商标识查询凭证
     *
     * @param provider 厂商标识
     * @return 凭证 PO，不存在返回 null
     */
    CredentialPO selectByProvider(@Param("provider") String provider);

    /**
     * 查询全部凭证
     *
     * @return 凭证 PO 列表
     */
    List<CredentialPO> selectAll();

    /**
     * 新增凭证（业务ID外部赋值）
     *
     * @param credentialPO 凭证 PO
     * @return 受影响行数
     */
    int insert(CredentialPO credentialPO);

    /**
     * 按厂商标识更新凭证
     *
     * @param credentialPO 凭证 PO
     * @return 受影响行数
     */
    int updateByProvider(CredentialPO credentialPO);

    /**
     * 按厂商标识删除凭证
     *
     * @param provider 厂商标识
     * @return 受影响行数
     */
    int deleteByProvider(@Param("provider") String provider);
}
