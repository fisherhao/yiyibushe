package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.app.service.PersonalAssetService;
import com.dayu.yiyibushe.common.ApiResult;
import com.dayu.yiyibushe.domain.asset.AssetCategory;
import com.dayu.yiyibushe.domain.asset.AssetItem;
import com.dayu.yiyibushe.web.interceptor.LoginUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 个人素材接口：上传、列出、删除本人图片。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final PersonalAssetService personalAssetService;

    /**
     * 构造器
     *
     * @param personalAssetService
     *                             个人素材服务
     */
    public AssetController(PersonalAssetService personalAssetService) {
        this.personalAssetService = personalAssetService;
    }

    /**
     * 上传图片
     *
     * @param category
     *                 分类：AVATAR/TOP/PANTS/SOCKS/SHOES/HAT
     * @param file
     *                 图片文件
     * @param request
     *                 当前请求
     * @return 素材项
     * @throws IOException 读写异常
     */
    @PostMapping("/upload")
    public ApiResult<AssetItem> upload(@RequestParam String category,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        AssetItem item = personalAssetService.uploadAsset(
                LoginUtil.currentUser(request), AssetCategory.parse(category), file);
        return ApiResult.success(item);
    }

    /**
     * 批量上传图片（单次 1~10 张，单张最大 10MB）
     *
     * @param category
     *                 分类：AVATAR/TOP/PANTS/SOCKS/SHOES/HAT
     * @param files
     *                 图片文件数组
     * @param request
     *                 当前请求
     * @return 本次上传的素材项列表
     * @throws IOException 读写异常
     */
    @PostMapping("/upload-batch")
    public ApiResult<List<AssetItem>> uploadBatch(@RequestParam String category,
            @RequestParam("files") MultipartFile[] files,
            HttpServletRequest request) throws IOException {
        List<AssetItem> itemList = personalAssetService.uploadAssets(
                LoginUtil.currentUser(request), AssetCategory.parse(category), files);
        return ApiResult.success(itemList);
    }

    /**
     * 列出某分类下本人图片
     *
     * @param category
     *                 分类
     * @param request
     *                 当前请求
     * @return 素材项列表
     */
    @GetMapping
    public ApiResult<List<AssetItem>> list(@RequestParam String category, HttpServletRequest request) {
        return ApiResult.success(personalAssetService.listAssets(
                LoginUtil.currentUser(request), AssetCategory.parse(category)));
    }

    /**
     * 删除本人图片
     *
     * @param assetId
     *                业务素材 ID
     * @param request
     *                当前请求
     * @return 成功标记
     * @throws IOException 读写异常
     */
    @DeleteMapping
    public ApiResult<String> delete(@RequestParam Long assetId, HttpServletRequest request) throws IOException {
        personalAssetService.deleteAsset(LoginUtil.currentUser(request), assetId);
        return ApiResult.success("已删除");
    }
}
