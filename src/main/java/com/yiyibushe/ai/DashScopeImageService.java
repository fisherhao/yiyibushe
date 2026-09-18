package com.yiyibushe.ai;

import java.util.List;

/**
 * DashScope 通义万相图像生成服务接口。
 * <p>
 * 业务代码依赖本接口，不直接依赖具体实现，便于替换模型厂商。
 *
 * @author Witty·Kid Fisher
 */
public interface DashScopeImageService {

    /**
     * 提交文生图任务。
     *
     * @param prompt        文字描述
     * @param refImageUrls  可选的参考图 URL
     * @return 任务 ID
     */
    String submitTextToImage(String prompt, List<String> refImageUrls);

    /**
     * 提交虚拟试衣任务。
     *
     * @param personUrl  人物形象图 URL
     * @param garmentUrl 服装图 URL
     * @return 任务 ID
     */
    String submitVirtualTryOn(String personUrl, String garmentUrl);

    /**
     * 轮询任务结果，返回最终图片 URL。
     *
     * @return 图片 URL；若超时或失败则抛 RuntimeException
     */
    String pollTaskUntilDone(String taskId);
}
