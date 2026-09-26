package com.dayu.yiyibushe.infra.ai.prompt;

import com.dayu.yiyibushe.common.exception.BizErrorCode;
import com.dayu.yiyibushe.common.exception.BizException;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.dao.mybatis.PromptMybatisMapper;
import com.dayu.yiyibushe.dao.po.PromptPO;
import com.dayu.yiyibushe.infra.ai.constant.AiConstants;
import com.dayu.yiyibushe.infra.ai.definition.PromptDefinition;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 提示词存储：全项目提示词的唯一运行期来源。
 * <p>
 * 加载与热更新策略（对齐资产缓存规范）：
 * <ul>
 *   <li>{@link #loadAll()} 启动时全量载入内存，代码全程只读内存、不打 DB；</li>
 *   <li>{@link #incrementalRefresh()} 每 30 秒按 version 增量拉取：改库内容并递增 version 后
 *       最长 30 秒生效，无需重启进程；status 置 INACTIVE 即下线；</li>
 *   <li>{@link #reload()} 全量重载，供种子装载后调用，也为未来 RocketMQ 广播失效预留入口。</li>
 * </ul>
 * 提示词内容支持 {@code {0}} 编号占位符，经 {@link #format} 用 {@link MessageFormat} 渲染。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class PromptStore {

    private static final Logger log = LogUtilExt.getLogger(PromptStore.class);

    /** 本地未缓存任何版本时的起始版本号 */
    private static final int NO_VERSION = 0;

    /** 提示词缓存：prompt_code -> 定义（只保留 ACTIVE） */
    private final Map<String, PromptDefinition> promptCache = new ConcurrentHashMap<>();

    /** 本地已缓存的最大版本号 */
    private final AtomicInteger maxVersion = new AtomicInteger(NO_VERSION);

    @Autowired
    private PromptMybatisMapper promptMybatisMapper;

    /**
     * 启动时全量加载提示词
     */
    @PostConstruct
    public void loadAll() {
        synchronized (this) {
            promptCache.clear();
            int currentMax = NO_VERSION;
            for (PromptPO promptPO : promptMybatisMapper.selectAll()) {
                cacheOrEvict(promptPO);
                currentMax = Math.max(currentMax, defaultVersion(promptPO.getVersion()));
            }
            maxVersion.set(currentMax);
            LogUtilExt.info(log, "[PromptStore] 全量加载完成，生效提示词 {0} 条，最大版本 {1}",
                    promptCache.size(), currentMax);
        }
    }

    /**
     * 定时增量拉取：version 大于本地最大版本的记录并入缓存
     */
    @Scheduled(fixedDelayString = "${ai.prompt.refresh-interval-millis:30000}",
            initialDelayString = "${ai.prompt.refresh-interval-millis:30000}")
    public void incrementalRefresh() {
        try {
            int currentMax = maxVersion.get();
            List<PromptPO> changedList = promptMybatisMapper.selectByVersionGreaterThan(currentMax);
            if (changedList.isEmpty()) {
                return;
            }
            int nextMax = currentMax;
            for (PromptPO promptPO : changedList) {
                cacheOrEvict(promptPO);
                nextMax = Math.max(nextMax, defaultVersion(promptPO.getVersion()));
            }
            maxVersion.set(nextMax);
            LogUtilExt.info(log, "[PromptStore] 增量刷新完成，变更 {0} 条，最大版本 {1}",
                    changedList.size(), nextMax);
        } catch (Exception e) {
            // 刷新失败只告警：沿用旧缓存，不影响业务链路
            LogUtilExt.error(log, "[PromptStore] 增量刷新失败: {0}", e.getMessage(), e);
        }
    }

    /**
     * 全量重载：种子装载完成或收到缓存失效广播时调用
     */
    public void reload() {
        loadAll();
    }

    /**
     * 按编码获取生效提示词原文
     *
     * @param promptCode 提示词编码
     * @return 提示词内容
     *
     * @throws BizException 提示词未配置或已下线时抛出
     */
    public String require(String promptCode) {
        PromptDefinition definition = promptCache.get(promptCode);
        if (Objects.isNull(definition)) {
            throw new BizException(BizErrorCode.PROMPT_MISSING);
        }
        return definition.getContent();
    }

    /**
     * 按编码获取提示词原文，缺失或下线时返回空串（不抛异常的宽松场景）
     *
     * @param promptCode 提示词编码
     * @return 提示词内容，不存在返回空串
     */
    public String getOrBlank(String promptCode) {
        PromptDefinition definition = promptCache.get(promptCode);
        return Objects.isNull(definition) ? "" : StringUtilExt.defaultString(definition.getContent());
    }

    /**
     * 按编码获取提示词并渲染编号占位符
     *
     * @param promptCode 提示词编码
     * @param args       占位符参数（按 {0}、{1} 顺序）
     * @return 渲染后的提示词
     *
     * @throws BizException 提示词未配置或已下线时抛出
     */
    public String format(String promptCode, Object... args) {
        return MessageFormat.format(require(promptCode), args);
    }

    /**
     * 按状态把记录放入缓存或从缓存移除
     *
     * @param promptPO 提示词 PO
     */
    private void cacheOrEvict(PromptPO promptPO) {
        if (StringUtilExt.equals(AiConstants.PROMPT_STATUS_INACTIVE, promptPO.getStatus())) {
            promptCache.remove(promptPO.getPromptCode());
        } else {
            promptCache.put(promptPO.getPromptCode(), toDefinition(promptPO));
        }
    }

    /**
     * PO 转领域定义
     *
     * @param promptPO 提示词 PO
     * @return 提示词定义
     */
    private static PromptDefinition toDefinition(PromptPO promptPO) {
        PromptDefinition definition = new PromptDefinition();
        definition.setPromptCode(promptPO.getPromptCode());
        definition.setPromptName(promptPO.getPromptName());
        definition.setCategory(promptPO.getCategory());
        definition.setContent(promptPO.getContent());
        definition.setStatus(promptPO.getStatus());
        definition.setVersion(promptPO.getVersion());
        definition.setRemark(promptPO.getRemark());
        return definition;
    }

    /**
     * 版本号空安全取值
     *
     * @param version 版本号
     * @return 非空版本号，null 返回起始版本
     */
    private static int defaultVersion(Integer version) {
        return Objects.isNull(version) ? NO_VERSION : version;
    }
}
