package com.dayu.yiyibushe.app.service;

import com.dayu.yiyibushe.domain.tryon.OutfitGenerateRequest;
import com.dayu.yiyibushe.domain.tryon.TryOnTaskResult;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;

/**
 * 套装生成服务接口：根据提示词与个人素材生成穿衣图片。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public interface OutfitService {

    /**
     * 提交套装生成（内部链式驱动试穿，结束后任务即为成功态）
     *
     * @param principal
     *     登录主体
     * @param request
     *     套装生成请求
     * @return 任务 ID
     */
    String submitOutfit(LoginPrincipal principal, OutfitGenerateRequest request);

    /**
     * 查询任务结果
     *
     * @param taskId
     *     任务 ID
     * @return 任务结果
     */
    TryOnTaskResult fetchTask(String taskId);
}
