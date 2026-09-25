package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.FunctionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 函数表 ai_function 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface FunctionMybatisMapper {

    /**
     * 按函数编码查询
     *
     * @param functionCode 函数编码
     * @return 函数 PO，不存在返回 null
     */
    FunctionPO selectByFunctionCode(@Param("functionCode") String functionCode);

    /**
     * 查询全部函数
     *
     * @return 函数 PO 列表
     */
    List<FunctionPO> selectAll();

    /**
     * 新增函数（业务ID外部赋值）
     *
     * @param functionPO 函数 PO
     * @return 受影响行数
     */
    int insert(FunctionPO functionPO);
}
