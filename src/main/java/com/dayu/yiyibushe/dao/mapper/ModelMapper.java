package com.dayu.yiyibushe.dao.mapper;

import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;

import java.util.List;

/**
 * AI 模型数据访问接口，查询一律按模型编码进行。
 * <p>
 * MySQL 实现为 {@code MysqlModelMapper}，负责 PO 与领域定义的转换。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface ModelMapper {

    /**
     * 按模型编码查询最高版本定义
     *
     * @param modelCode 模型编码
     * @return 模型定义，不存在返回 null
     */
    ModelDefinition selectByModelCode(String modelCode);

    /**
     * 查询全部模型定义
     *
     * @return 模型定义列表
     */
    List<ModelDefinition> selectAll();

    /**
     * 新增模型
     *
     * @param modelDefinition 模型定义
     * @return 影响行数
     */
    int insert(ModelDefinition modelDefinition);

    /**
     * 按业务ID更新模型
     *
     * @param modelDefinition 模型定义
     * @return 影响行数
     */
    int updateByModelId(ModelDefinition modelDefinition);
}
