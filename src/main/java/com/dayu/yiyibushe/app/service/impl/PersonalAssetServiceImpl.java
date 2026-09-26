package com.dayu.yiyibushe.app.service.impl;

import com.dayu.yiyibushe.app.service.PersonalAssetService;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.dao.mybatis.AssetMybatisMapper;
import com.dayu.yiyibushe.dao.mybatis.AssetPO;
import com.dayu.yiyibushe.domain.asset.AssetCategory;
import com.dayu.yiyibushe.domain.asset.AssetItem;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import com.dayu.yiyibushe.infra.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 个人素材服务实现。
 * <p>
 * 图片文件本体存本地（local-resource/assets），访问链接与归属关系落 asset 表：
 * user_id 绑定归属人、category 绑定衣物类别（人物图/帽子/上衣/裤子/袜子/鞋子），
 * 列表与容量校验都走数据库。
 * <p>
 * 每个用户每个分类最多保存 {@link #MAX_ASSET_PER_CATEGORY} 张；
 * 单次批量上传最多 {@link #MAX_UPLOAD_PER_REQUEST} 张；
 * 删除时按库记录校验归属，防止越权操作他人文件。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@Service
public class PersonalAssetServiceImpl implements PersonalAssetService {

    /** 每人每类图片上限 */
    public static final int MAX_ASSET_PER_CATEGORY = 10;

    /** 单次批量上传张数上限 */
    public static final int MAX_UPLOAD_PER_REQUEST = 10;

    /** 图片有效期（天）：过期后列表不再展示 */
    private static final int ASSET_EXPIRE_DAYS = 7;

    @Autowired
    private StorageService storageService;

    /** 素材表 Mapper（XML 形式） */
    @Autowired
    private AssetMybatisMapper assetMybatisMapper;

    /**
     * 上传一张图片
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
    @Override
    public AssetItem uploadAsset(LoginPrincipal principal, AssetCategory category, MultipartFile file)
            throws IOException {
        List<AssetItem> uploadedList = uploadAssets(principal, category, new MultipartFile[]{file});
        return uploadedList.get(0);
    }

    /**
     * 批量上传图片：先校验张数、空文件与分类容量，全部通过后逐张落盘并落库
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
    @Override
    public List<AssetItem> uploadAssets(LoginPrincipal principal, AssetCategory category, MultipartFile[] files)
            throws IOException {
        if (Objects.isNull(files) || files.length == 0) {
            throw new BizException(BizErrorCode.UPLOAD_FILE_EMPTY);
        }
        if (files.length > MAX_UPLOAD_PER_REQUEST) {
            throw new BizException(BizErrorCode.UPLOAD_BATCH_EXCEEDED);
        }
        for (MultipartFile file : files) {
            if (Objects.isNull(file) || file.isEmpty()) {
                throw new BizException(BizErrorCode.UPLOAD_FILE_EMPTY);
            }
        }
        int ownedCount = assetMybatisMapper.countByUserIdAndCategory(principal.userId(), category.name());
        if (ownedCount + files.length > MAX_ASSET_PER_CATEGORY) {
            throw new BizException(BizErrorCode.ASSET_LIMIT_EXCEEDED);
        }
        List<AssetItem> uploadedList = new ArrayList<>();
        for (MultipartFile file : files) {
            // 先落文件本体，再发号并把链接与归属关系落 asset 表
            AssetItem uploaded = storageService.upload(principal.userId(), category, file);
            AssetItem saved = saveAssetRecord(principal.userId(), category, uploaded);
            uploadedList.add(saved);
        }
        return uploadedList;
    }

    /**
     * 列出某用户某分类下的图片（从 asset 表查询，不再扫文件目录）
     *
     * @param principal
     *     登录主体
     * @param category
     *     素材分类
     * @return 素材项列表
     */
    @Override
    public List<AssetItem> listAssets(LoginPrincipal principal, AssetCategory category) {
        List<AssetPO> assetPOList = assetMybatisMapper.selectByUserIdAndCategory(
                principal.userId(), category.name());
        return CollectionUtilExt.mapToList(assetPOList, PersonalAssetServiceImpl::toItem);
    }

    /**
     * 删除本人图片：按业务素材 ID 查库校验归属后，先删 asset 表记录，再删本地文件
     *
     * @param principal
     *     登录主体
     * @param assetId
     *     业务素材 ID
     * @throws IOException 读写异常
     */
    @Override
    public void deleteAsset(LoginPrincipal principal, Long assetId) throws IOException {
        AssetPO assetPO = assetMybatisMapper.selectByAssetId(assetId);
        if (Objects.isNull(assetPO)) {
            throw new BizException(BizErrorCode.ASSET_NOT_FOUND);
        }
        if (!Objects.equals(assetPO.getUserId(), principal.userId())) {
            throw new BizException(BizErrorCode.ASSET_NOT_OWNER);
        }
        assetMybatisMapper.deleteByAssetId(assetId);
        storageService.delete(assetPO.getObjectKey());
    }

    /**
     * 把上传结果落 asset 表：业务 asset_id 由 {@link IdUtil} 按 userId 发 18 位分段 ID
     * （主键 id 数据库自增），过期时间按上传时间 + {@link #ASSET_EXPIRE_DAYS} 天计算，
     * 插入后返回带 assetId 的素材项
     *
     * @param userId
     *     归属用户 ID
     * @param category
     *     素材分类
     * @param uploaded
     *     存储服务返回的上传结果（assetId 为 null）
     * @return 已落库的素材项
     */
    private AssetItem saveAssetRecord(Long userId, AssetCategory category, AssetItem uploaded) {
        AssetPO assetPO = new AssetPO();
        assetPO.setAssetId(IdUtil.nextId(userId));
        assetPO.setUserId(userId);
        assetPO.setCategory(category.name());
        assetPO.setUrl(uploaded.url());
        assetPO.setObjectKey(uploaded.key());
        assetPO.setFileName(uploaded.fileName());
        assetPO.setFileSize(uploaded.size());
        assetPO.setGmtExpire(LocalDateTime.now().plusDays(ASSET_EXPIRE_DAYS));
        assetMybatisMapper.insert(assetPO);
        return new AssetItem(assetPO.getAssetId(), assetPO.getObjectKey(), assetPO.getUrl(),
                category.name(), assetPO.getFileName(), uploaded.size(), userId);
    }

    /**
     * 把素材持久化对象还原成素材项
     *
     * @param assetPO
     *     素材持久化对象
     * @return 素材项
     */
    private static AssetItem toItem(AssetPO assetPO) {
        return new AssetItem(assetPO.getAssetId(), assetPO.getObjectKey(), assetPO.getUrl(),
                assetPO.getCategory(), assetPO.getFileName(),
                Optional.ofNullable(assetPO.getFileSize()).orElse(0L), assetPO.getUserId());
    }
}
