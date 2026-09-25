package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.ToolPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 工具表 ai_tool 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface ToolMybatisMapper {

    /**
     * 按工具编码查询最高版本记录
     *
     * @param toolCode 工具编码
     * @return 工具 PO，不存在返回 null
     */
    ToolPO selectByToolCode(@Param("toolCode") String toolCode);

    /**
     * 查询全部工具
     *
     * @return 工具 PO 列表
     */
    List<ToolPO> selectAll();

    /**
     * 按绑定函数业务ID查询工具
     *
     * @param functionId 函数业务ID
     * @return 工具 PO 列表
     */
    List<ToolPO> selectByFunctionId(@Param("functionId") Long functionId);

    /**
     * 新增工具（业务ID外部赋值）
     *
     * @param toolPO 工具 PO
     * @return 受影响行数
     */
    int insert(ToolPO toolPO);

    /**
     * 按业务ID更新工具
     *
     * @param toolPO 工具 PO
     * @return 受影响行数
     */
    int updateByToolId(ToolPO toolPO);
}
