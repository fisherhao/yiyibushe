package com.dayu.yiyibushe.dao.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 说明：图片素材表 asset 的 MyBatis XML Mapper。
 * <p>
 * 图片文件存本地、链接与归属关系落本表：user_id 绑定归属人、category 绑定衣物类别；
 * 主键 id 无语义、由发号器生成，业务素材 ID 为 asset_id。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Mapper
public interface AssetMybatisMapper {

    /**
     * 新增素材记录（主键 id 与业务 asset_id 均已由发号器在外部赋值；时间列由数据库默认填充）
     *
     * @param assetPO
     *                待新增素材记录
     * @return 受影响行数
     */
    int insert(AssetPO assetPO);

    /**
     * 按归属用户与分类查询素材（按 gmt_create 升序，先上传的在前）
     *
     * @param userId
     *                归属用户 ID
     * @param category
     *                素材分类名
     * @return 素材记录列表
     */
    List<AssetPO> selectByUserIdAndCategory(@Param("userId") Long userId,
            @Param("category") String category);

    /**
     * 统计某用户某分类下的素材数量（每类上限校验用）
     *
     * @param userId
     *                归属用户 ID
     * @param category
     *                素材分类名
     * @return 素材数量
     */
    int countByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 按业务素材 ID 查询单条素材（删除时校验归属用）
     *
     * @param assetId
     *                业务素材 ID
     * @return 素材记录，不存在返回 null
     */
    AssetPO selectByAssetId(@Param("assetId") Long assetId);

    /**
     * 按业务素材 ID 删除素材记录
     *
     * @param assetId
     *                业务素材 ID
     * @return 受影响行数
     */
    int deleteByAssetId(@Param("assetId") Long assetId);
}
