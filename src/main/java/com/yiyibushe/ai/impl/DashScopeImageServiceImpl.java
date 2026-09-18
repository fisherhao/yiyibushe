package com.yiyibushe.ai.impl;

import com.yiyibushe.ai.DashScopeImageService;
import com.yiyibushe.ai.DashScopeProperties;
import com.yiyibushe.common.exception.BizException;
import com.yiyibushe.common.exception.ErrorCode;
import com.yiyibushe.common.utils.CollectionsUtilsExt;
import com.yiyibushe.common.utils.JsonUtilsExt;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

/**
 * DashScope 通义万相图像生成服务实现。
 * <p>
 * 所有 Wanx 系列接口都是「提交异步任务 -> 轮询结果」模式。
 *
 * @author Witty·Kid Fisher
 */
@Service
public class DashScopeImageServiceImpl implements DashScopeImageService {

    private final RestClient restClient;
    private final DashScopeProperties properties;

    public DashScopeImageServiceImpl(RestClient dashScopeRestClient, DashScopeProperties properties) {
        this.restClient = dashScopeRestClient;
        this.properties = properties;
    }

    @Override
    public String submitTextToImage(String prompt, List<String> refImageUrls) {
        JSONObject input = JsonUtilsExt.newObject();
        input.put("prompt", prompt);
        if (CollectionsUtilsExt.isNotEmpty(refImageUrls)) {
            JSONArray refs = JsonUtilsExt.newArray();
            for (String url : refImageUrls) {
                refs.add(JsonUtilsExt.put(JsonUtilsExt.newObject(), "image", url));
            }
            input.put("ref_img", refs);
        }

        JSONObject body = JsonUtilsExt.newObject();
        body.put("model", properties.getTextToImageModel());
        body.put("input", input);
        JSONObject parameters = JsonUtilsExt.newObject();
        parameters.put("size", "1024*1024");
        parameters.put("n", 1);
        body.put("parameters", parameters);

        String taskId = postForTaskId("/api/v1/services/aigc/text2image/image-synthesis", body);
        System.out.println("文生图任务已提交: model=" + properties.getTextToImageModel() + ", taskId=" + taskId);
        return taskId;
    }

    @Override
    public String submitVirtualTryOn(String personUrl, String garmentUrl) {
        JSONObject input = JsonUtilsExt.newObject();
        input.put("person_image_url", personUrl);
        input.put("garment_image_url", garmentUrl);

        JSONObject body = JsonUtilsExt.newObject();
        body.put("model", properties.getVirtualTryonModel());
        body.put("input", input);

        String taskId = postForTaskId("/api/v1/services/aigc/virtualtryon/image-generation", body);
        System.out.println("虚拟试衣任务已提交: model=" + properties.getVirtualTryonModel() + ", taskId=" + taskId);
        return taskId;
    }

    @Override
    public String pollTaskUntilDone(String taskId) {
        long deadline = System.currentTimeMillis() + properties.getPollTimeoutMs();
        while (System.currentTimeMillis() < deadline) {
            JSONObject resp = restClient.get()
                    .uri("/api/v1/tasks/{taskId}", taskId)
                    .retrieve()
                    .body(JSONObject.class);
            String status = readStatus(resp);
            System.out.println("轮询任务 " + taskId + " 状态=" + status);
            switch (status) {
                case "SUCCEEDED":
                    return readFirstImageUrl(resp);
                case "FAILED":
                    throw new BizException(ErrorCode.DASHSCOPE_TASK_FAILED,
                            "任务失败: " + JsonUtilsExt.toJsonString(resp));
                default:
                    // PENDING / RUNNING / UNKNOWN 继续轮询
                    sleep(properties.getPollIntervalMs());
            }
        }
        throw new BizException(ErrorCode.DASHSCOPE_TASK_TIMEOUT, "任务超时未完成: taskId=" + taskId);
    }

    private String postForTaskId(String path, JSONObject body) {
        ResponseEntity<String> resp = restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toJSONString())
                .retrieve()
                .toEntity(String.class);
        JSONObject json = JsonUtilsExt.parseObject(resp.getBody());
        JSONObject output = json.getJSONObject("output");
        if (Objects.isNull(output)) {
            throw new BizException(ErrorCode.DASHSCOPE_RESPONSE_INVALID,
                    "DashScope 返回缺少 output 字段: " + resp.getBody());
        }
        String taskId = output.getString("task_id");
        if (Objects.isNull(taskId)) {
            throw new BizException(ErrorCode.DASHSCOPE_RESPONSE_INVALID,
                    "DashScope 未返回 task_id: " + resp.getBody());
        }
        return taskId;
    }

    private String readStatus(JSONObject resp) {
        JSONObject output = resp.getJSONObject("output");
        if (Objects.isNull(output)) {
            throw new BizException(ErrorCode.DASHSCOPE_RESPONSE_INVALID,
                    "任务查询响应缺 output: " + JsonUtilsExt.toJsonString(resp));
        }
        String status = output.getString("task_status");
        return Objects.isNull(status) ? "UNKNOWN" : status.toUpperCase();
    }

    private String readFirstImageUrl(JSONObject resp) {
        JSONObject output = resp.getJSONObject("output");
        Object resultsObj = output.get("results");
        if (resultsObj instanceof JSONArray array && !array.isEmpty()) {
            String url = array.getJSONObject(0).getString("url");
            if (Objects.isNull(url)) {
                url = array.getJSONObject(0).getString("b64_image");
            }
            return url;
        }
        String url = output.getString("url");
        if (Objects.nonNull(url)) {
            return url;
        }
        throw new BizException(ErrorCode.DASHSCOPE_RESPONSE_INVALID,
                "任务成功但未找到结果图: " + JsonUtilsExt.toJsonString(resp));
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
