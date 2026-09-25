package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.ModelPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 模型表 ai_model 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface ModelMybatisMapper {

    /**
     * 按模型编码查询最高版本记录
     *
     * @param modelCode 模型编码
     * @return 模型 PO，不存在返回 null
     */
    ModelPO selectByModelCode(@Param("modelCode") String modelCode);

    /**
     * 查询全部模型（按业务ID升序）
     *
     * @return 模型 PO 列表
     */
    List<ModelPO> selectAll();

    /**
     * 新增模型（业务ID外部赋值，时间列数据库填充）
     *
     * @param modelPO 模型 PO
     * @return 受影响行数
     */
    int insert(ModelPO modelPO);

    /**
     * 按业务ID更新模型
     *
     * @param modelPO 模型 PO
     * @return 受影响行数
     */
    int updateByModelId(ModelPO modelPO);
}
