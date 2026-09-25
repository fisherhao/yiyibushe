package com.dayu.yiyibushe.infra.ai.connection;

import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型连接池：按模型 code 持有 AgentScope {@link Model}（其内部含 HTTP 连接池）。
 * <p>
 * 启动时池为空、不发起任何大模型连接——连接只在以下时机建立：
 * <ol>
 *   <li>控制平台新增/启用模型并把助手引用切到该模型时，调 {@link #initialize(String)} 立即建连入池；</li>
 *   <li>对话时 {@link #get(String)} 发现池中没有，首次发问兜底建连。</li>
 * </ol>
 * 连接建立后一直复用；模型下线/切换时调 {@link #evict(String)} 移除。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class ModelConnectionPool {

    /** key = 模型 code，value = 含连接池的 Model */
    private final Map<String, Model> pool = new ConcurrentHashMap<>();

    @Autowired
    private ModelRegistry modelRegistry;

    @Autowired
    private AgentScopeModelFactory modelFactory;

    /**
     * 立即初始化模型连接并入池（控制平台启用/切换模型时调用）；已存在则直接复用
     *
     * @param modelCode
     *     模型编码
     * @return 模型连接
     */
    public Model initialize(String modelCode) {
        return pool.computeIfAbsent(modelCode, this::createModel);
    }

    /**
     * 取模型连接；池中没有时首次发问兜底建立
     *
     * @param modelCode
     *     模型编码
     * @return 模型连接
     */
    public Model get(String modelCode) {
        return pool.computeIfAbsent(modelCode, this::createModel);
    }

    /**
     * 移除模型连接（模型下线/切换），下次使用时自动重建
     *
     * @param modelCode
     *     模型编码
     */
    public void evict(String modelCode) {
        pool.remove(modelCode);
    }

    /**
     * 经工厂为指定模型建立连接
     *
     * @param modelCode
     *     模型编码
     * @return 模型连接
     */
    private Model createModel(String modelCode) {
        ModelDefinition definition = modelRegistry.require(modelCode);
        return modelFactory.createChatModel(definition);
    }
}
