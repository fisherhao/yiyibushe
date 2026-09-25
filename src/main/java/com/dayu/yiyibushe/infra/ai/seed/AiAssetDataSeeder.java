package com.dayu.yiyibushe.infra.ai.seed;

import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.JsonUtilExt;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.dao.mapper.FunctionMapper;
import com.dayu.yiyibushe.dao.mapper.ModelMapper;
import com.dayu.yiyibushe.dao.mapper.PluginInstallMapper;
import com.dayu.yiyibushe.dao.mapper.PluginMapper;
import com.dayu.yiyibushe.dao.mapper.SkillMapper;
import com.dayu.yiyibushe.dao.mapper.ToolMapper;
import com.dayu.yiyibushe.dao.mybatis.CredentialMybatisMapper;
import com.dayu.yiyibushe.dao.po.CredentialPO;
import com.dayu.yiyibushe.dao.po.PluginInstallPO;
import com.dayu.yiyibushe.infra.ai.credential.ApiCredential;
import com.dayu.yiyibushe.infra.ai.credential.LocalCredentialMigrationSource;
import com.dayu.yiyibushe.infra.ai.definition.FunctionDefinition;
import com.dayu.yiyibushe.infra.ai.definition.PluginDefinition;
import com.dayu.yiyibushe.infra.ai.definition.SkillDefinition;
import com.dayu.yiyibushe.infra.ai.definition.ToolDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocument;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocumentLoader;
import io.agentscope.core.tool.AgentTool;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 资产种子数据装载器：应用启动、Flyway 建表完成后，把内置基线数据灌入数据库。
 * <p>
 * 全部步骤幂等（按编码判重，已存在则跳过），任一步失败仅告警不阻断启动：
 * <ol>
 *   <li>凭证：环境变量/本地配置中已有的厂商密钥迁入 ai_credential；</li>
 *   <li>模型：内置 10 个默认模型迁入 ai_model；</li>
 *   <li>函数：4 个 NATIVE 函数迁入 ai_function；</li>
 *   <li>工具：从容器中 4 个 AgentTool Bean 提取元数据迁入 ai_tool；</li>
 *   <li>技能：classpath 的 SKILL.md（天气/新闻）解析后迁入 ai_skill；</li>
 *   <li>插件与安装实例：天气/新闻两个插件及其工作空间安装。</li>
 * </ol>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class AiAssetDataSeeder implements ApplicationRunner {

    private static final Logger log = LogUtilExt.getLogger(AiAssetDataSeeder.class);

    /** 凭证启用状态 */
    private static final String STATUS_ENABLED = "ENABLED";

    /** 函数启用状态 */
    private static final String FUNCTION_STATUS_ENABLED = "ENABLED";

    /** 发布状态：已发布 */
    private static final String PUBLISH_STATUS_PUBLISHED = "PUBLISHED";

    /** 运行状态：可用 */
    private static final String RUNTIME_STATUS_ACTIVE = "ACTIVE";

    /** 可见范围：公开 */
    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    /** 来源：本地文件 */
    private static final String SOURCE_TYPE_LOCAL_FILE = "LOCAL_FILE";

    /** NATIVE 执行器类型 */
    private static final String EXECUTOR_TYPE_NATIVE = "NATIVE";

    /** 工作空间范围 */
    private static final String SCOPE_TYPE_WORKSPACE = "WORKSPACE";

    /** 安装状态：已安装 */
    private static final String INSTALL_STATUS_INSTALLED = "INSTALLED";

    /** 默认工作空间业务ID（种子常量） */
    private static final long DEFAULT_WORKSPACE_ID = 1L;

    /** 迁入凭证备注 */
    private static final String CREDENTIAL_REMARK_MIGRATED = "启动时从环境变量/本地配置迁入";

    @Autowired
    private ModelRegistry modelRegistry;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FunctionMapper functionMapper;

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private PluginMapper pluginMapper;

    @Autowired
    private PluginInstallMapper pluginInstallMapper;

    @Autowired
    private CredentialMybatisMapper credentialMybatisMapper;

    @Autowired
    private LocalCredentialMigrationSource localCredentialMigrationSource;

    @Autowired
    private SkillDocumentLoader skillDocumentLoader;

    /** 容器中全部 AgentTool Bean（定位/天气/新闻/加载指令 4 个） */
    @Autowired
    private List<AgentTool> agentTools;

    @Override
    public void run(ApplicationArguments args) {
        seedCredentials();
        seedModels();
        seedFunctions();
        seedTools();
        seedSkills();
        seedPluginsAndInstalls();
    }

    /**
     * 迁入环境变量/本地配置中已有的厂商凭证。
     */
    private void seedCredentials() {
        int migratedCount = 0;
        for (String provider : localCredentialMigrationSource.listProviders()) {
            try {
                if (Objects.nonNull(credentialMybatisMapper.selectByProvider(provider))) {
                    continue;
                }
                ApiCredential credential = localCredentialMigrationSource.getCredential(provider);
                if (Objects.isNull(credential)) {
                    continue;
                }
                CredentialPO credentialPO = new CredentialPO();
                credentialPO.setCredentialId(IdUtil.nextId());
                credentialPO.setProvider(provider);
                credentialPO.setAppKey(credential.getAppKey());
                credentialPO.setAppSecret(credential.getAppSecret());
                credentialPO.setStatus(STATUS_ENABLED);
                credentialPO.setRemark(CREDENTIAL_REMARK_MIGRATED);
                credentialMybatisMapper.insert(credentialPO);
                migratedCount++;
            } catch (Exception e) {
                LogUtilExt.error(log, "[AiSeed] 凭证迁入失败 provider={0}, {1}",
                        provider, e.getMessage(), e);
            }
        }
        LogUtilExt.info(log, "[AiSeed] 凭证迁入完成，新增 {0} 条", migratedCount);
    }

    /**
     * 迁入内置默认模型。
     */
    private void seedModels() {
        int migratedCount = 0;
        for (ModelDefinition defaultModel : modelRegistry.listBuiltInDefaults()) {
            try {
                if (Objects.nonNull(modelMapper.selectByModelCode(defaultModel.getCode()))) {
                    continue;
                }
                modelMapper.insert(defaultModel);
                migratedCount++;
            } catch (Exception e) {
                LogUtilExt.error(log, "[AiSeed] 模型迁入失败 code={0}, {1}",
                        defaultModel.getCode(), e.getMessage(), e);
            }
        }
        LogUtilExt.info(log, "[AiSeed] 模型迁入完成，新增 {0} 条", migratedCount);
        // 模型落库后刷新注册表：首次启动时 @PostConstruct 早于种子装载，库为空，必须重载
        modelRegistry.reloadFromDatabase();
    }

    /**
     * 迁入 4 个 NATIVE 函数。
     */
    private void seedFunctions() {
        int migratedCount = 0;
        for (FunctionSeedSpec spec : buildFunctionSeedSpecs()) {
            try {
                if (Objects.nonNull(functionMapper.selectByFunctionCode(spec.functionCode()))) {
                    continue;
                }
                FunctionDefinition definition = new FunctionDefinition();
                definition.setFunctionCode(spec.functionCode());
                definition.setFunctionName(spec.functionName());
                definition.setDescription(spec.description());
                definition.setExecutorType(EXECUTOR_TYPE_NATIVE);
                definition.setExecutorConfig(buildNativeExecutorConfig(spec.beanName()));
                definition.setStatus(FUNCTION_STATUS_ENABLED);
                functionMapper.insert(definition);
                migratedCount++;
            } catch (Exception e) {
                LogUtilExt.error(log, "[AiSeed] 函数迁入失败 code={0}, {1}",
                        spec.functionCode(), e.getMessage(), e);
            }
        }
        LogUtilExt.info(log, "[AiSeed] 函数迁入完成，新增 {0} 条", migratedCount);
    }

    /**
     * 从容器 AgentTool Bean 提取元数据，迁入工具表并绑定函数。
     */
    private void seedTools() {
        int migratedCount = 0;
        for (AgentTool agentTool : agentTools) {
            try {
                ToolSeedSpec spec = resolveToolSeedSpec(agentTool.getName());
                if (Objects.isNull(spec)) {
                    continue;
                }
                if (Objects.nonNull(toolMapper.selectByToolCode(agentTool.getName()))) {
                    continue;
                }
                FunctionDefinition boundFunction = functionMapper.selectByFunctionCode(spec.functionCode());
                if (Objects.isNull(boundFunction)) {
                    LogUtilExt.warn(log, "[AiSeed] 工具绑定的函数不存在，跳过 tool={0}, function={1}",
                            agentTool.getName(), spec.functionCode());
                    continue;
                }
                ToolDefinition definition = new ToolDefinition();
                definition.setToolCode(agentTool.getName());
                definition.setFunctionId(boundFunction.getFunctionId());
                definition.setDisplayName(spec.displayName());
                definition.setDescription(agentTool.getDescription());
                definition.setInputSchema(JsonUtilExt.toJsonString(agentTool.getParameters()));
                definition.setCategory(spec.category());
                definition.setTimeoutSeconds(spec.timeoutSeconds());
                definition.setRetryCount(1);
                definition.setCacheTtlSeconds(spec.cacheTtlSeconds());
                definition.setPublishStatus(PUBLISH_STATUS_PUBLISHED);
                definition.setRuntimeStatus(RUNTIME_STATUS_ACTIVE);
                toolMapper.insert(definition);
                migratedCount++;
            } catch (Exception e) {
                LogUtilExt.error(log, "[AiSeed] 工具迁入失败 tool={0}, {1}",
                        agentTool.getName(), e.getMessage(), e);
            }
        }
        LogUtilExt.info(log, "[AiSeed] 工具迁入完成，新增 {0} 条", migratedCount);
    }

    /**
     * 把 classpath 的 SKILL.md 解析结果迁入技能表，工具引用按工具业务ID组装。
     */
    private void seedSkills() {
        int migratedCount = 0;
        for (SkillDocument skillDocument : skillDocumentLoader.listSkills()) {
            try {
                SkillSeedSpec spec = resolveSkillSeedSpec(skillDocument.name());
                if (Objects.isNull(spec)) {
                    continue;
                }
                if (Objects.nonNull(skillMapper.selectBySkillCode(skillDocument.name()))) {
                    continue;
                }
                SkillDefinition definition = new SkillDefinition();
                definition.setSkillCode(skillDocument.name());
                definition.setDisplayName(spec.displayName());
                definition.setDescription(skillDocument.description());
                definition.setInstructions(skillDocument.instructions());
                definition.setTools(buildSkillToolsJson(spec.toolCodes()));
                definition.setCategory(spec.category());
                definition.setCompatibility(spec.compatibility());
                definition.setMetadataJson(spec.metadataJson());
                definition.setSortNo(spec.sortNo());
                definition.setPublishStatus(PUBLISH_STATUS_PUBLISHED);
                definition.setRuntimeStatus(RUNTIME_STATUS_ACTIVE);
                skillMapper.insert(definition);
                migratedCount++;
            } catch (Exception e) {
                LogUtilExt.error(log, "[AiSeed] 技能迁入失败 skill={0}, {1}",
                        skillDocument.name(), e.getMessage(), e);
            }
        }
        LogUtilExt.info(log, "[AiSeed] 技能迁入完成，新增 {0} 条", migratedCount);
    }

    /**
     * 迁入天气/新闻两个插件及对应工作空间安装实例。
     */
    private void seedPluginsAndInstalls() {
        seedPlugin(buildWeatherPluginSpec());
        seedPlugin(buildTechNewsPluginSpec());
    }

    /**
     * 迁入单个插件并创建安装实例（已存在则跳过）。
     *
     * @param spec 插件种子规格
     */
    private void seedPlugin(PluginSeedSpec spec) {
        try {
            PluginDefinition existedPlugin = pluginMapper.selectByPluginCode(spec.pluginCode());
            PluginDefinition pluginDefinition;
            if (Objects.nonNull(existedPlugin)) {
                pluginDefinition = existedPlugin;
            } else {
                pluginDefinition = new PluginDefinition();
                pluginDefinition.setPluginCode(spec.pluginCode());
                pluginDefinition.setDisplayName(spec.displayName());
                pluginDefinition.setDescription(spec.description());
                pluginDefinition.setVersion(spec.version());
                pluginDefinition.setAuthor("fisherhao");
                pluginDefinition.setOwner("fisherhao");
                pluginDefinition.setVisibility(VISIBILITY_PUBLIC);
                pluginDefinition.setSourceType(SOURCE_TYPE_LOCAL_FILE);
                pluginDefinition.setSourceRef(spec.sourceRef());
                pluginDefinition.setCategory(spec.category());
                pluginDefinition.setItems(buildPluginItemsJson(spec.skillCode()));
                pluginDefinition.setPermissionConfig(spec.permissionConfig());
                pluginDefinition.setCredentialRequired(false);
                pluginDefinition.setPublishStatus(PUBLISH_STATUS_PUBLISHED);
                pluginDefinition.setRuntimeStatus(RUNTIME_STATUS_ACTIVE);
                pluginMapper.insert(pluginDefinition);
                LogUtilExt.info(log, "[AiSeed] 插件迁入完成: {0}", spec.pluginCode());
            }

            List<PluginInstallPO> scopeInstalls = pluginInstallMapper
                    .selectByScope(SCOPE_TYPE_WORKSPACE, DEFAULT_WORKSPACE_ID);
            PluginInstallPO matchedInstall = CollectionUtilExt.findFirst(scopeInstalls,
                    install -> Objects.equals(install.getPluginId(), pluginDefinition.getPluginId()));
            if (Objects.nonNull(matchedInstall)) {
                return;
            }
            PluginInstallPO installPO = new PluginInstallPO();
            installPO.setPluginId(pluginDefinition.getPluginId());
            installPO.setPluginCode(pluginDefinition.getPluginCode());
            installPO.setVersion(pluginDefinition.getVersion());
            installPO.setScopeType(SCOPE_TYPE_WORKSPACE);
            installPO.setScopeId(DEFAULT_WORKSPACE_ID);
            installPO.setInstallStatus(INSTALL_STATUS_INSTALLED);
            pluginInstallMapper.insert(installPO);
        } catch (Exception e) {
            LogUtilExt.error(log, "[AiSeed] 插件迁入失败 plugin={0}, {1}",
                    spec.pluginCode(), e.getMessage(), e);
        }
    }

    /**
     * 构建 NATIVE 执行器配置 JSON：{"bean":"..."}，方法默认 callAsync（P3 执行器统一反射调用）。
     *
     * @param beanName Spring 容器 Bean 名
     * @return 配置 JSON
     */
    private String buildNativeExecutorConfig(String beanName) {
        Map<String, Object> configMap = new LinkedHashMap<>();
        configMap.put("bean", beanName);
        return JsonUtilExt.toJsonString(configMap);
    }

    /**
     * 组装技能工具引用 JSON：[{"toolId":...,"toolCode":...,"required":true,"order":1}]
     *
     * @param toolCodes 按顺序排列的工具编码
     * @return 工具引用 JSON
     */
    private String buildSkillToolsJson(List<String> toolCodes) {
        List<Map<String, Object>> toolRefs = new ArrayList<>();
        int order = 1;
        for (String toolCode : toolCodes) {
            ToolDefinition toolDefinition = toolMapper.selectByToolCode(toolCode);
            if (Objects.isNull(toolDefinition)) {
                continue;
            }
            Map<String, Object> toolRef = new LinkedHashMap<>();
            toolRef.put("toolId", toolDefinition.getToolId());
            toolRef.put("toolCode", toolCode);
            toolRef.put("required", true);
            toolRef.put("order", order++);
            toolRefs.add(toolRef);
        }
        return JsonUtilExt.toJsonString(toolRefs);
    }

    /**
     * 组装插件内容清单 JSON：[{"itemType":"SKILL","itemId":...,"required":true}]
     *
     * @param skillCode 技能编码
     * @return 内容清单 JSON
     */
    private String buildPluginItemsJson(String skillCode) {
        SkillDefinition skillDefinition = skillMapper.selectBySkillCode(skillCode);
        List<Map<String, Object>> items = new ArrayList<>();
        if (Objects.nonNull(skillDefinition)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemType", "SKILL");
            item.put("itemId", skillDefinition.getSkillId());
            item.put("itemCode", skillCode);
            item.put("required", true);
            items.add(item);
        }
        return JsonUtilExt.toJsonString(items);
    }

    /**
     * 构建函数种子规格列表。
     *
     * @return 函数规格列表
     */
    private List<FunctionSeedSpec> buildFunctionSeedSpecs() {
        return List.of(
                new FunctionSeedSpec("native-current-location", "定位当前位置",
                        "按网络出口 IP 定位当前城市与经纬度", "getCurrentLocationTool"),
                new FunctionSeedSpec("native-get-weather", "查询实时天气",
                        "按城市名或经纬度查询实时天气", "getWeatherTool"),
                new FunctionSeedSpec("native-get-tech-news", "获取科技新闻",
                        "抓取最新科技新闻条目", "getTechNewsTool"),
                new FunctionSeedSpec("native-load-skill-instructions", "加载技能指令",
                        "按技能名加载 SOP 正文", "loadSkillInstructionsTool"));
    }

    /**
     * 天气插件种子规格。
     *
     * @return 插件规格
     */
    private PluginSeedSpec buildWeatherPluginSpec() {
        Map<String, Object> permissionMap = new LinkedHashMap<>();
        permissionMap.put("network", List.of("ip-api.com", "open-meteo.com", "wttr.in"));
        permissionMap.put("dangerousOperations", false);
        return new PluginSeedSpec("weather-plugin", "天气查询插件",
                "查询指定城市或当前位置的实时天气", "1.0.0",
                "skills/current-weather/SKILL.md", "weather", "current-weather",
                JsonUtilExt.toJsonString(permissionMap));
    }

    /**
     * 新闻插件种子规格。
     *
     * @return 插件规格
     */
    private PluginSeedSpec buildTechNewsPluginSpec() {
        Map<String, Object> permissionMap = new LinkedHashMap<>();
        permissionMap.put("network", List.of("api.gdeltproject.org", "en.wikinews.org",
                "hacker-news.firebaseio.com"));
        permissionMap.put("dangerousOperations", false);
        return new PluginSeedSpec("tech-news-plugin", "科技新闻插件",
                "获取并整理今日科技新闻速递", "1.0.0",
                "skills/daily-tech-news/SKILL.md", "news", "daily-tech-news",
                JsonUtilExt.toJsonString(permissionMap));
    }

    /**
     * 按工具编码解析工具种子规格，未收录返回 null。
     *
     * @param toolCode 工具编码
     * @return 工具规格或 null
     */
    private ToolSeedSpec resolveToolSeedSpec(String toolCode) {
        return switch (toolCode) {
            case "get-current-location" -> new ToolSeedSpec("native-current-location",
                    "当前位置定位", "location", 10, 0);
            case "get-weather" -> new ToolSeedSpec("native-get-weather",
                    "实时天气查询", "weather", 15, 0);
            case "get-tech-news" -> new ToolSeedSpec("native-get-tech-news",
                    "科技新闻获取", "news", 20, 600);
            case "load-skill-instructions" -> new ToolSeedSpec("native-load-skill-instructions",
                    "技能指令加载", "system", 5, 0);
            default -> null;
        };
    }

    /**
     * 按技能编码解析技能种子规格，未收录返回 null。
     *
     * @param skillCode 技能编码
     * @return 技能规格或 null
     */
    private SkillSeedSpec resolveSkillSeedSpec(String skillCode) {
        return switch (skillCode) {
            case "current-weather" -> new SkillSeedSpec("当前位置天气查询",
                    List.of("get-current-location", "get-weather"), "weather",
                    "需要访问外网（ip-api.com、open-meteo.com，均为免费开源服务，无需 API Key）",
                    buildSkillMetadataJson(), 1);
            case "daily-tech-news" -> new SkillSeedSpec("每日科技新闻速递",
                    List.of("get-tech-news"), "news",
                    "需要访问外网（api.gdeltproject.org、en.wikinews.org、hacker-news.firebaseio.com，均免费，无需 API Key）",
                    buildSkillMetadataJson(), 2);
            default -> null;
        };
    }

    /**
     * 构建技能元数据 JSON：{"author":"fisherhao","version":"1.0","standard":"agentskills.io"}
     *
     * @return 元数据 JSON
     */
    private String buildSkillMetadataJson() {
        Map<String, Object> metadataMap = new LinkedHashMap<>();
        metadataMap.put("author", "fisherhao");
        metadataMap.put("version", "1.0");
        metadataMap.put("standard", "agentskills.io");
        return JsonUtilExt.toJsonString(metadataMap);
    }

    /**
     * 函数种子规格（内部值对象）。
     *
     * @param functionCode 函数编码
     * @param functionName 函数名称
     * @param description  函数说明
     * @param beanName     NATIVE Bean 名
     */
    private record FunctionSeedSpec(String functionCode, String functionName,
                                    String description, String beanName) {
    }

    /**
     * 工具种子规格。
     *
     * @param functionCode    绑定函数编码
     * @param displayName     展示名称
     * @param category        分类
     * @param timeoutSeconds  超时秒数
     * @param cacheTtlSeconds 缓存秒数
     */
    private record ToolSeedSpec(String functionCode, String displayName, String category,
                                int timeoutSeconds, int cacheTtlSeconds) {
    }

    /**
     * 技能种子规格。
     *
     * @param displayName   展示名称
     * @param toolCodes     工具编码（按顺序）
     * @param category      分类
     * @param compatibility 运行依赖说明
     * @param metadataJson  元数据 JSON
     * @param sortNo        排序号
     */
    private record SkillSeedSpec(String displayName, List<String> toolCodes, String category,
                                 String compatibility, String metadataJson, int sortNo) {
    }

    /**
     * 插件种子规格。
     *
     * @param pluginCode       插件编码
     * @param displayName      展示名称
     * @param description      能力描述
     * @param version          语义化版本
     * @param sourceRef        来源引用
     * @param category         分类
     * @param skillCode        内含技能编码
     * @param permissionConfig 权限声明 JSON
     */
    private record PluginSeedSpec(String pluginCode, String displayName, String description,
                                  String version, String sourceRef, String category,
                                  String skillCode, String permissionConfig) {
    }
}
