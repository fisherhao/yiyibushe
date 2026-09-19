package com.dayu.yiyibushe.app.service;

import com.dayu.yiyibushe.domain.asset.AssetCategory;
import com.dayu.yiyibushe.domain.asset.AssetItem;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 个人素材服务接口：管理当前登录用户的人物图与各类衣物图片。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface PersonalAssetService {

    /**
     * 上传一张图片，受「每人每类上限」约束
     *
     * @param principal
     *     登录主体
     * @param category
     *     素材分类
     * @param file
     *     图片文件
     * @return 素材项
     * @throws IOException 读写异常
     */
    AssetItem uploadAsset(LoginPrincipal principal, AssetCategory category, MultipartFile file) throws IOException;

    /**
     * 批量上传图片：单次 1~10 张，受「每人每类上限」约束，任一校验不通过则整批拒绝
     *
     * @param principal
     *     登录主体
     * @param category
     *     素材分类
     * @param files
     *     图片文件数组
     * @return 本次上传的素材项列表
     * @throws IOException 读写异常
     */
    List<AssetItem> uploadAssets(LoginPrincipal principal, AssetCategory category, MultipartFile[] files)
            throws IOException;

    /**
     * 列出某用户某分类下的图片
     *
     * @param principal
     *     登录主体
     * @param category
     *     素材分类
     * @return 素材项列表
     */
    List<AssetItem> listAssets(LoginPrincipal principal, AssetCategory category);

    /**
     * 删除本人图片（按业务素材 ID，校验归属后删库记录与本地文件）
     *
     * @param principal
     *     登录主体
     * @param assetId
     *     业务素材 ID
     * @throws IOException 读写异常
     */
    void deleteAsset(LoginPrincipal principal, Long assetId) throws IOException;
}
