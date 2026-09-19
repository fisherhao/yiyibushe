package com.dayu.yiyibushe.app.service.impl;

import com.dayu.yiyibushe.app.service.OutfitService;
import com.dayu.yiyibushe.app.service.PersonalAssetService;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.domain.asset.AssetCategory;
import com.dayu.yiyibushe.domain.asset.AssetItem;
import com.dayu.yiyibushe.domain.tryon.OutfitGenerateRequest;
import com.dayu.yiyibushe.domain.tryon.TryOnTaskResult;
import com.dayu.yiyibushe.infra.ai.core.AiResponse;
import com.dayu.yiyibushe.infra.ai.service.AiPlatformService;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import com.dayu.yiyibushe.infra.storage.StorageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 套装生成服务实现。
 * <p>
 * 流程：确定人物图（指定/最新/按提示词文生图）-> 确定各部位衣物（指定/自动最新）
 * -> 链式虚拟试穿：上一步输出图作为下一步人物图。
 * 链式调用会阻塞到全部完成，因此返回时任务已是成功态。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Service
public class OutfitServiceImpl implements OutfitService {

    /** 文生图模型编码 */
    private static final String TEXT_TO_IMAGE_MODEL = "dashscope-wanx-t2i";

    /** 虚拟试穿模型编码 */
    private static final String VIRTUAL_TRYON_MODEL = "dashscope-virtualtryon";

    /** 衣物参与试穿的固定顺序 */
    private static final List<AssetCategory> GARMENT_ORDER = List.of(AssetCategory.TOP, AssetCategory.PANTS,
            AssetCategory.SOCKS,
            AssetCategory.SHOES, AssetCategory.HAT);

    private final AiPlatformService aiPlatformService;
    private final StorageService storageService;
    private final PersonalAssetService personalAssetService;

    /** 任务结果存储（内存，未来可换 Redis） */
    private final Map<String, TryOnTaskResult> resultStore = new ConcurrentHashMap<>();

    /**
     * 构造器
     *
     * @param aiPlatformService
     *                             AI 平台服务
     * @param storageService
     *                             存储服务
     * @param personalAssetService
     *                             个人素材服务（素材列表从 asset 表查）
     */
    public OutfitServiceImpl(AiPlatformService aiPlatformService, StorageService storageService,
            PersonalAssetService personalAssetService) {
        this.aiPlatformService = aiPlatformService;
        this.storageService = storageService;
        this.personalAssetService = personalAssetService;
    }

    /**
     * 提交套装生成：解析人物图与衣物后按固定顺序链式虚拟试穿，阻塞返回最终任务 ID
     *
     * @param principal
     *                  登录主体
     * @param request
     *                  套装请求
     * @return 最终试穿任务 ID
     */
    @Override
    public String submitOutfit(LoginPrincipal principal, OutfitGenerateRequest request) {
        String personImageUrl = resolvePersonImageUrl(principal, request);
        List<String> garmentUrls = resolveGarmentUrls(principal, request);
        if (CollectionUtilExt.isEmpty(garmentUrls)) {
            throw new BizException(BizErrorCode.OUTFIT_NO_CLOTHES);
        }
        System.out.println("[Outfit] 开始链式试穿，衣物数量=" + CollectionUtilExt.getSize(garmentUrls));
        String lastTaskId = null;
        for (String garmentUrl : garmentUrls) {
            lastTaskId = aiPlatformService.submitVirtualTryOn(
                    VIRTUAL_TRYON_MODEL, personImageUrl, garmentUrl);
            AiResponse response = aiPlatformService.waitTask(VIRTUAL_TRYON_MODEL, lastTaskId);
            if (CollectionUtilExt.isNotEmpty(response.getImageUrls())) {
                personImageUrl = response.getImageUrls().get(0);
            }
        }
        TryOnTaskResult success = TryOnTaskResult.succeeded(lastTaskId, personImageUrl, personImageUrl);
        resultStore.put(lastTaskId, success);
        return lastTaskId;
    }

    /**
     * 查询任务结果
     *
     * @param taskId
     *               任务 ID
     * @return 任务结果
     */
    @Override
    public TryOnTaskResult fetchTask(String taskId) {
        TryOnTaskResult result = resultStore.get(taskId);
        if (Objects.isNull(result)) {
            throw new BizException(BizErrorCode.TASK_NOT_FOUND);
        }
        return result;
    }

    /**
     * 确定人物图 URL：显式指定 > 最新人物图 > 按提示词文生图生成
     *
     * @param principal
     *                  登录主体
     * @param request
     *                  套装请求
     * @return 人物图 URL
     */
    private String resolvePersonImageUrl(LoginPrincipal principal, OutfitGenerateRequest request) {
        if (StringUtilExt.isNotBlank(request.personKey())) {
            ensureOwner(principal, request.personKey());
            return storageService.publicUrl(request.personKey());
        }
        List<AssetItem> avatars = personalAssetService.listAssets(principal, AssetCategory.AVATAR);
        if (CollectionUtilExt.isNotEmpty(avatars)) {
            return avatars.get(CollectionUtilExt.getSize(avatars) - 1).url();
        }
        // 没有人物图：按提示词生成一张符合场景的人物形象图
        String prompt = StringUtilExt.isBlank(request.prompt())
                ? "一位穿着时尚的年轻人，全身照，真实摄影风格"
                : request.prompt() + "，人物全身照，真实摄影风格";
        System.out.println("[Outfit] 无人物图，按提示词文生图");
        return aiPlatformService.generateImage(TEXT_TO_IMAGE_MODEL, prompt, null).get(0);
    }

    /**
     * 确定参与试穿的衣物 URL：显式指定优先，未指定的分类自动取最新一件
     *
     * @param principal
     *                  登录主体
     * @param request
     *                  套装请求
     * @return 衣物 URL 列表（按固定顺序）
     */
    private List<String> resolveGarmentUrls(LoginPrincipal principal, OutfitGenerateRequest request) {
        List<String> garmentUrls = new ArrayList<>();
        for (AssetCategory category : GARMENT_ORDER) {
            String explicitKey = pickExplicitKey(request, category);
            if (Objects.nonNull(explicitKey)) {
                ensureOwner(principal, explicitKey);
                garmentUrls.add(storageService.publicUrl(explicitKey));
                continue;
            }
            List<AssetItem> storedItems = personalAssetService.listAssets(principal, category);
            if (CollectionUtilExt.isNotEmpty(storedItems)) {
                garmentUrls.add(storedItems.get(CollectionUtilExt.getSize(storedItems) - 1).url());
            }
        }
        return garmentUrls;
    }

    /**
     * 从请求中取某分类的显式衣物 key
     *
     * @param request
     *                 套装请求
     * @param category
     *                 分类
     * @return key，未指定返回 null
     */
    private String pickExplicitKey(OutfitGenerateRequest request, AssetCategory category) {
        return switch (category) {
            case TOP -> request.topKey();
            case PANTS -> request.pantsKey();
            case SOCKS -> request.socksKey();
            case SHOES -> request.shoesKey();
            case HAT -> request.hatKey();
            case AVATAR -> null;
        };
    }

    /**
     * 校验 key 属于当前用户
     *
     * @param principal
     *                  登录主体
     * @param key
     *                  存储对象 key
     */
    private void ensureOwner(LoginPrincipal principal, String key) {
        if (!StringUtilExt.startsWith(key, principal.userId() + "/")) {
            throw new BizException(BizErrorCode.ASSET_OWNER_MISMATCH);
        }
    }
}
