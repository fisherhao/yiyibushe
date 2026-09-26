package com.dayu.yiyibushe.dao.mybatis;

import com.dayu.yiyibushe.dao.po.PromptPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 提示词表 ai_prompt 的 MyBatis Mapper，SQL 全部写在 XML。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface PromptMybatisMapper {

    /**
     * 查询全部提示词
     *
     * @return 提示词 PO 列表
     */
    List<PromptPO> selectAll();

    /**
     * 按提示词编码查询
     *
     * @param promptCode 提示词编码
     * @return 提示词 PO，不存在返回 null
     */
    PromptPO selectByPromptCode(@Param("promptCode") String promptCode);

    /**
     * 查询版本号大于给定值的全部提示词（增量拉取用）
     *
     * @param version 本地已缓存的最大版本号
     * @return 提示词 PO 列表
     */
    List<PromptPO> selectByVersionGreaterThan(@Param("version") int version);

    /**
     * 按分类查询提示词
     *
     * @param category 分类编码
     * @return 提示词 PO 列表
     */
    List<PromptPO> selectByCategory(@Param("category") String category);

    /**
     * 新增提示词
     *
     * @param promptPO 提示词 PO
     * @return 受影响行数
     */
    int insert(PromptPO promptPO);

    /**
     * 更新提示词内容并递增版本号
     *
     * @param promptCode 提示词编码
     * @param content    新内容
     * @return 受影响行数
     */
    int updateContent(@Param("promptCode") String promptCode, @Param("content") String content);

    /**
     * 更新提示词状态并递增版本号
     *
     * @param promptCode 提示词编码
     * @param status     新状态
     * @return 受影响行数
     */
    int updateStatus(@Param("promptCode") String promptCode, @Param("status") String status);
}
