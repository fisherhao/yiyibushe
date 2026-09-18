package com.yiyibushe.tryon;

import com.yiyibushe.asset.AssetCategory;
import com.yiyibushe.asset.AssetItem;
import com.yiyibushe.common.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 试穿与素材的 HTTP 接口。
 * <p>
 * 接口一览：
 * <pre>
 *   POST   /api/assets/upload?category=TOP              上传素材
 *   GET    /api/assets?category=TOP                     列出某类素材
 *   POST   /api/tryon/text2image                        文生图任务
 *   POST   /api/tryon/virtualtryon                      虚拟试衣任务
 *   GET    /api/tryon/tasks/{taskId}                    查询任务结果
 *   GET    /api/share?url=xxx                           生成分享 URL
 * </pre>
 *
 * @author Witty·Kid Fisher
 */
@RestController
@RequestMapping("/api")
public class VirtualTryOnController {

    private final VirtualTryOnService tryOnService;
    private final ShareService shareService;

    public VirtualTryOnController(VirtualTryOnService tryOnService, ShareService shareService) {
        this.tryOnService = tryOnService;
        this.shareService = shareService;
    }

    @PostMapping("/assets/upload")
    public ApiResult<AssetItem> upload(@RequestParam String category,
                                      @RequestParam("file") MultipartFile file) throws IOException {
        AssetItem item = tryOnService.uploadAsset(AssetCategory.parse(category), file);
        return ApiResult.ok(item);
    }

    @GetMapping("/assets")
    public ApiResult<List<AssetItem>> list(@RequestParam String category) {
        return ApiResult.ok(tryOnService.listAssets(AssetCategory.parse(category)));
    }

    @PostMapping("/tryon/text2image")
    public ApiResult<Map<String, String>> textToImage(@RequestBody TextToImageRequest request) {
        String taskId = tryOnService.submitTextToImage(request);
        return ApiResult.ok(Map.of("taskId", taskId));
    }

    @PostMapping("/tryon/virtualtryon")
    public ApiResult<Map<String, String>> virtualTryOn(@RequestBody VirtualTryOnRequest request) {
        String taskId = tryOnService.submitVirtualTryOn(request);
        return ApiResult.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/tryon/tasks/{taskId}")
    public ApiResult<TryOnTaskResult> getTask(@PathVariable String taskId) {
        return ApiResult.ok(tryOnService.fetchTaskResult(taskId));
    }

    @GetMapping("/share")
    public ApiResult<String> share(@RequestParam String url) {
        return ApiResult.ok(shareService.buildShareUrl(url));
    }
}
