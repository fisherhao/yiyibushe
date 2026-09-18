package com.yiyibushe.tryon;

import com.yiyibushe.asset.AssetCategory;
import com.yiyibushe.asset.AssetItem;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 试穿业务编排接口。
 * <p>
 * 业务代码依赖本接口，不直接依赖具体实现。
 *
 * @author Witty·Kid Fisher
 */
public interface VirtualTryOnService {

    /**
     * 路径 A：文生图。用户文字描述生成符合人体态的形象图。
     */
    String submitTextToImage(TextToImageRequest request);

    /**
     * 路径 B：虚拟试衣。人物图 + 多件服装链式合成穿衣图。
     */
    String submitVirtualTryOn(VirtualTryOnRequest request);

    /**
     * 同步等待任务结果（演示用，直接阻塞）。
     */
    TryOnTaskResult fetchTaskResult(String taskId);

    /**
     * 列出指定分类下的素材。
     */
    List<AssetItem> listAssets(AssetCategory category);

    /**
     * 上传一张素材图片。
     */
    AssetItem uploadAsset(AssetCategory category, MultipartFile file) throws IOException;
}
