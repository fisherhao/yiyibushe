package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.common.ApiResult;
import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.dao.mapper.PromptMapper;
import com.dayu.yiyibushe.infra.ai.definition.PromptDefinition;
import com.dayu.yiyibushe.web.dto.PromptCreateRequest;
import com.dayu.yiyibushe.web.dto.PromptUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 提示词管理接口：对 {@code ai_prompt} 表提供 CRUD，修改内容时数据库自增 version
 * 触发 {@code PromptStore} 定时增量拉取，实现秒级热生效、无需重启。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    @Autowired
    private PromptMapper promptMapper;

    /**
     * 查询提示词列表，可按分类过滤
     *
     * @param category 分类编码（可选）
     * @return 提示词定义列表
     */
    @GetMapping
    public ApiResult<List<PromptDefinition>> list(
            @RequestParam(value = "category", required = false) String category) {
        List<PromptDefinition> result = StringUtilExt.isBlank(category)
                ? promptMapper.selectAll()
                : promptMapper.selectByCategory(category);
        return ApiResult.success(result);
    }

    /**
     * 按编码查询提示词详情
     *
     * @param promptCode 提示词编码
     * @return 提示词定义
     */
    @GetMapping("/{promptCode}")
    public ApiResult<PromptDefinition> detail(@PathVariable String promptCode) {
        PromptDefinition definition = promptMapper.selectByPromptCode(promptCode);
        if (Objects.isNull(definition)) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        return ApiResult.success(definition);
    }

    /**
     * 新增提示词
     *
     * @param request 新增请求
     * @return 新增的提示词定义
     */
    @PostMapping
    public ApiResult<PromptDefinition> create(@RequestBody PromptCreateRequest request) {
        if (Objects.isNull(request) || StringUtilExt.isBlank(request.getPromptCode())) {
            throw new BizException(BizErrorCode.PROMPT_MISSING);
        }
        PromptDefinition definition = new PromptDefinition();
        definition.setPromptCode(request.getPromptCode());
        definition.setPromptName(request.getPromptName());
        definition.setCategory(request.getCategory());
        definition.setContent(request.getContent());
        definition.setRemark(request.getRemark());
        promptMapper.insert(definition);
        return ApiResult.success(promptMapper.selectByPromptCode(request.getPromptCode()));
    }

    /**
     * 修改提示词内容（version 自增，30 秒内触发热更新）
     *
     * @param promptCode 提示词编码
     * @param request    修改请求
     * @return 修改后的提示词定义
     */
    @PutMapping("/{promptCode}")
    public ApiResult<PromptDefinition> update(@PathVariable String promptCode,
            @RequestBody PromptUpdateRequest request) {
        if (Objects.isNull(request) || StringUtilExt.isBlank(request.getContent())) {
            throw new BizException(BizErrorCode.PROMPT_MISSING);
        }
        int affected = promptMapper.updateContent(promptCode, request.getContent());
        if (affected == 0) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        return ApiResult.success(promptMapper.selectByPromptCode(promptCode));
    }

    /**
     * 切换提示词上下线状态（version 自增，30 秒内触发热更新）
     *
     * @param promptCode 提示词编码
     * @param status     目标状态：ACTIVE / INACTIVE
     * @return 修改后的提示词定义
     */
    @PutMapping("/{promptCode}/status")
    public ApiResult<PromptDefinition> updateStatus(@PathVariable String promptCode,
            @RequestParam String status) {
        int affected = promptMapper.updateStatus(promptCode, status);
        if (affected == 0) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        return ApiResult.success(promptMapper.selectByPromptCode(promptCode));
    }
}
