package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.app.service.OutfitService;
import com.dayu.yiyibushe.app.service.ShareService;
import com.dayu.yiyibushe.common.ApiResult;
import com.dayu.yiyibushe.domain.tryon.OutfitGenerateRequest;
import com.dayu.yiyibushe.domain.tryon.TryOnTaskResult;
import com.dayu.yiyibushe.web.interceptor.LoginUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 套装生成接口：提交生成、查询结果、生成分享链接。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/outfit")
public class OutfitController {

    @Autowired
    private OutfitService outfitService;

    @Autowired
    private ShareService shareService;

    /**
     * 提交套装生成
     *
     * @param request
     *     套装请求
     * @param httpRequest
     *     当前请求
     * @return 任务 ID
     */
    @PostMapping("/generate")
    public ApiResult<Map<String, String>> generate(@RequestBody OutfitGenerateRequest request,
                                                   HttpServletRequest httpRequest) {
        String taskId = outfitService.submitOutfit(LoginUtil.currentUser(httpRequest), request);
        return ApiResult.success(Map.of("taskId", taskId));
    }

    /**
     * 查询任务结果
     *
     * @param taskId
     *     任务 ID
     * @return 任务结果
     */
    @GetMapping("/tasks/{taskId}")
    public ApiResult<TryOnTaskResult> getTask(@PathVariable String taskId) {
        return ApiResult.success(outfitService.fetchTask(taskId));
    }

    /**
     * 生成分享链接
     *
     * @param url
     *     图片 URL 或 key
     * @return 可访问链接
     */
    @GetMapping("/share")
    public ApiResult<String> share(@RequestParam String url) {
        return ApiResult.success(shareService.buildShareUrl(url));
    }
}
