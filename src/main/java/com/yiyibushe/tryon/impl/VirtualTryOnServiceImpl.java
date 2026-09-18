package com.yiyibushe.tryon.impl;

import com.yiyibushe.ai.DashScopeImageService;
import com.yiyibushe.asset.AssetCategory;
import com.yiyibushe.asset.AssetItem;
import com.yiyibushe.asset.OssService;
import com.yiyibushe.common.exception.BizException;
import com.yiyibushe.common.exception.ErrorCode;
import com.yiyibushe.common.utils.CollectionsUtilsExt;
import com.yiyibushe.tryon.TextToImageRequest;
import com.yiyibushe.tryon.TryOnTaskResult;
import com.yiyibushe.tryon.VirtualTryOnRequest;
import com.yiyibushe.tryon.VirtualTryOnService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 试穿业务编排实现。
 * <p>
 * 文生图：用户文字描述 -> 调通义万相 wanx2.1-t2i-turbo 生成图
 * 虚拟试衣：用户人物图 + 多件服装 -> 链式调 wanx-virtualtryon 合成穿衣图
 *
 * @author Witty·Kid Fisher
 */
@Service
public class VirtualTryOnServiceImpl implements VirtualTryOnService {

    private final DashScopeImageService imageService;
    private final OssService ossService;
    /** 任务结果内存存储，便于演示查询。生产环境替换为 Redis。 */
    private final Map<String, TryOnTaskResult> taskStore = new ConcurrentHashMap<>();

    public VirtualTryOnServiceImpl(DashScopeImageService imageService, OssService ossService) {
        this.imageService = imageService;
        this.ossService = ossService;
    }

    @Override
    public String submitTextToImage(TextToImageRequest request) {
        // toStream 内部已判空，refAssetKeys 为 null 时返回空 Stream，不会抛 NPE
        List<String> refUrls = CollectionsUtilsExt.toStream(request.refAssetKeys())
                .map(ossService::publicUrl)
                .toList();
        String taskId = imageService.submitTextToImage(request.prompt(), refUrls.isEmpty() ? null : refUrls);
        taskStore.put(taskId, TryOnTaskResult.pending(taskId));
        return taskId;
    }

    @Override
    public String submitVirtualTryOn(VirtualTryOnRequest request) {
        String personUrl = ossService.publicUrl(request.personKey());
        TryOnChain chain = new TryOnChain(personUrl);
        if (Objects.nonNull(request.topKey())) {
            chain.add(ossService.publicUrl(request.topKey()));
        }
        if (Objects.nonNull(request.pantsKey())) {
            chain.add(ossService.publicUrl(request.pantsKey()));
        }
        if (Objects.nonNull(request.shoesKey())) {
            chain.add(ossService.publicUrl(request.shoesKey()));
        }
        if (Objects.nonNull(request.hatKey())) {
            chain.add(ossService.publicUrl(request.hatKey()));
        }
        if (chain.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "至少需要一件服装素材");
        }
        String finalTaskId = chain.run(imageService);
        taskStore.put(finalTaskId, TryOnTaskResult.pending(finalTaskId));
        return finalTaskId;
    }

    @Override
    public TryOnTaskResult fetchTaskResult(String taskId) {
        TryOnTaskResult cached = taskStore.get(taskId);
        if (Objects.nonNull(cached) && ("SUCCEEDED".equals(cached.status()) || "FAILED".equals(cached.status()))) {
            return cached;
        }
        try {
            String imageUrl = imageService.pollTaskUntilDone(taskId);
            TryOnTaskResult ok = TryOnTaskResult.succeeded(taskId, imageUrl, imageUrl);
            taskStore.put(taskId, ok);
            return ok;
        } catch (RuntimeException e) {
            TryOnTaskResult fail = TryOnTaskResult.failed(taskId, e.getMessage());
            taskStore.put(taskId, fail);
            return fail;
        }
    }

    @Override
    public List<AssetItem> listAssets(AssetCategory category) {
        return ossService.list(category);
    }

    @Override
    public AssetItem uploadAsset(AssetCategory category, MultipartFile file) throws IOException {
        return ossService.upload(category, file);
    }

    /**
     * 内部链式试穿器：每次用「上一步人物图 URL」与「下一步服装图 URL」提交试衣任务并等待。
     * 上一步的输出图作为下一步的人物图，实现多件服装叠加。
     */
    private static class TryOnChain {

        private final List<String> garmentUrls = new ArrayList<>();
        private final String idPrefix;
        private String currentPersonUrl;

        TryOnChain(String personUrl) {
            this.idPrefix = "tryon-" + System.currentTimeMillis() + "-";
            this.currentPersonUrl = personUrl;
        }

        void add(String garmentUrl) {
            garmentUrls.add(garmentUrl);
        }

        boolean isEmpty() {
            return garmentUrls.isEmpty();
        }

        String run(DashScopeImageService client) {
            int step = 0;
            String lastTaskId = idPrefix + "0";
            for (String garmentUrl : garmentUrls) {
                step++;
                String realTaskId = client.submitVirtualTryOn(currentPersonUrl, garmentUrl);
                System.out.println("链式试穿 step=" + step + ", taskId=" + realTaskId);
                currentPersonUrl = client.pollTaskUntilDone(realTaskId);
                lastTaskId = realTaskId;
            }
            return lastTaskId;
        }
    }
}
