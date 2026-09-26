package com.dayu.yiyibushe.infra.ai.seed;

import com.dayu.yiyibushe.common.id.IdUtil;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.JsonUtilExt;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.dao.mapper.FunctionMapper;
import com.dayu.yiyibushe.dao.mapper.ModelMapper;
import com.dayu.yiyibushe.dao.mapper.PluginInstallMapper;
import com.dayu.yiyibushe.dao.mapper.PluginMapper;
import com.dayu.yiyibushe.dao.mapper.PromptMapper;
import com.dayu.yiyibushe.dao.mapper.SkillMapper;
import com.dayu.yiyibushe.dao.mapper.ToolMapper;
import com.dayu.yiyibushe.dao.mybatis.CredentialMybatisMapper;
import com.dayu.yiyibushe.dao.po.CredentialPO;
import com.dayu.yiyibushe.dao.po.PluginInstallPO;
import com.dayu.yiyibushe.infra.ai.constant.AiConstants;
import com.dayu.yiyibushe.infra.ai.credential.ApiCredential;
import com.dayu.yiyibushe.infra.ai.credential.LocalCredentialMigrationSource;
import com.dayu.yiyibushe.infra.ai.definition.FunctionDefinition;
import com.dayu.yiyibushe.infra.ai.definition.PluginDefinition;
import com.dayu.yiyibushe.infra.ai.definition.PromptDefinition;
import com.dayu.yiyibushe.infra.ai.definition.SkillDefinition;
import com.dayu.yiyibushe.infra.ai.definition.ToolDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ModelDefinition;
import com.dayu.yiyibushe.infra.ai.registry.ModelRegistry;
import com.dayu.yiyibushe.infra.ai.prompt.PromptStore;
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
    private PromptMapper promptMapper;

    @Autowired
    private PromptStore promptStore;

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
        seedPrompts();
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
                credentialPO.setStatus(AiConstants.STATUS_ENABLED);
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
                definition.setExecutorType(AiConstants.EXECUTOR_TYPE_NATIVE);
                definition.setExecutorConfig(buildNativeExecutorConfig(spec.beanName()));
                definition.setStatus(AiConstants.STATUS_ENABLED);
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
                definition.setPublishStatus(AiConstants.PUBLISH_STATUS_PUBLISHED);
                definition.setRuntimeStatus(AiConstants.RUNTIME_STATUS_ACTIVE);
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
                definition.setPublishStatus(AiConstants.PUBLISH_STATUS_PUBLISHED);
                definition.setRuntimeStatus(AiConstants.RUNTIME_STATUS_ACTIVE);
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
                pluginDefinition.setVisibility(AiConstants.VISIBILITY_PUBLIC);
                pluginDefinition.setSourceType(AiConstants.SOURCE_TYPE_LOCAL_FILE);
                pluginDefinition.setSourceRef(spec.sourceRef());
                pluginDefinition.setCategory(spec.category());
                pluginDefinition.setItems(buildPluginItemsJson(spec.skillCode()));
                pluginDefinition.setPermissionConfig(spec.permissionConfig());
                pluginDefinition.setCredentialRequired(false);
                pluginDefinition.setPublishStatus(AiConstants.PUBLISH_STATUS_PUBLISHED);
                pluginDefinition.setRuntimeStatus(AiConstants.RUNTIME_STATUS_ACTIVE);
                pluginMapper.insert(pluginDefinition);
                LogUtilExt.info(log, "[AiSeed] 插件迁入完成: {0}", spec.pluginCode());
            }

            List<PluginInstallPO> scopeInstalls = pluginInstallMapper
                    .selectByScope(AiConstants.SCOPE_TYPE_WORKSPACE, AiConstants.DEFAULT_WORKSPACE_ID);
            PluginInstallPO matchedInstall = CollectionUtilExt.findFirst(scopeInstalls,
                    install -> Objects.equals(install.getPluginId(), pluginDefinition.getPluginId()));
            if (Objects.nonNull(matchedInstall)) {
                return;
            }
            PluginInstallPO installPO = new PluginInstallPO();
            installPO.setPluginId(pluginDefinition.getPluginId());
            installPO.setPluginCode(pluginDefinition.getPluginCode());
            installPO.setVersion(pluginDefinition.getVersion());
            installPO.setScopeType(AiConstants.SCOPE_TYPE_WORKSPACE);
            installPO.setScopeId(AiConstants.DEFAULT_WORKSPACE_ID);
            installPO.setInstallStatus(AiConstants.INSTALL_STATUS_INSTALLED);
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
            item.put("itemType", AiConstants.ITEM_TYPE_SKILL);
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
                AiConstants.SKILL_CLASSPATH_DIRECTORY + "/current-weather/" + AiConstants.SKILL_FILE_NAME,
                "weather", "current-weather",
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
                AiConstants.SKILL_CLASSPATH_DIRECTORY + "/daily-tech-news/" + AiConstants.SKILL_FILE_NAME,
                "news", "daily-tech-news",
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
     * 灌入内置提示词基线（按 prompt_code 判重，已存在跳过），完成后全量重载缓存。
     */
    private void seedPrompts() {
        int insertedCount = 0;
        for (PromptDefinition promptDefinition : buildBaselinePrompts()) {
            try {
                if (Objects.nonNull(promptMapper.selectByPromptCode(promptDefinition.getPromptCode()))) {
                    continue;
                }
                promptMapper.insert(promptDefinition);
                insertedCount++;
            } catch (Exception e) {
                LogUtilExt.error(log, "[Seeder] 提示词 {0} 灌入失败: {1}",
                        promptDefinition.getPromptCode(), e.getMessage(), e);
            }
        }
        promptStore.reload();
        LogUtilExt.info(log, "[Seeder] 提示词种子完成，新增 {0} 条", insertedCount);
    }

    /**
     * 构建内置提示词基线：原散落在各 Agent/Tool/Loader 中的提示词与固定文案全部收口于此。
     *
     * @return 提示词定义列表
     */
    private List<PromptDefinition> buildBaselinePrompts() {
        List<PromptDefinition> baselineList = new ArrayList<>();
        baselineList.add(definePrompt(AiConstants.PROMPT_ASSISTANT_SYSTEM, "统一对话助手系统提示",
                AiConstants.PROMPT_CATEGORY_SYSTEM,
                "你是一个中文助手。你的能力严格限定在以下两个范畴，多一步都不要做：\n"
                        + "范畴一：天气查询（当前位置或指定城市的天气、气温、湿度、会不会下雨等）。\n"
                        + "范畴二：科技新闻速递（今日科技新闻、科技圈动态、科技界最新消息）。\n"
                        + "执行规则（严格遵守）：\n"
                        + "1. 先对照下方技能目录，判断用户请求命中哪个技能；\n"
                        + "2. 命中后必须先调用 load-skill-instructions 工具（入参 skillName 为目录中的技能名）"
                        + "加载该技能的完整执行指令，再按指令调用其业务工具；未加载指令前不得直接调用业务工具；\n"
                        + "3. 用户一句话同时命中两个技能时，两个技能的指令分别加载、业务工具全部调用，"
                        + "再把两部分结果合并到一条回复中，不能只答一半；\n"
                        + "4. 请求与天气、科技新闻都无关（例如讲笑话、问时间、闲聊、编程问题等）时，"
                        + "不要调用任何工具；\n"
                        + "5. 禁止编造工具返回中没有的事实。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_SKILL_METADATA_HEADER, "技能目录头部",
                AiConstants.PROMPT_CATEGORY_SYSTEM,
                "以下是可用的技能，用户请求与描述匹配时按技能指令执行：\n"));
        baselineList.add(definePrompt(AiConstants.PROMPT_PLAN_SYSTEM, "规划 Agent 系统提示",
                AiConstants.PROMPT_CATEGORY_SYSTEM,
                "你是专业穿搭规划师。根据用户描述的风格、场景和效果，"
                        + "结合用户上传的人物图与衣物，输出一份简洁的搭配方案：包含选用哪些衣物、搭配顺序与预期效果。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_EXECUTE_SYSTEM, "执行 Agent 系统提示",
                AiConstants.PROMPT_CATEGORY_SYSTEM,
                "你是穿搭执行师。请把规划方案转成一份可直接执行的穿衣合成说明，"
                        + "明确每一步使用的衣物类型与画面要求，语言简洁。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_REVIEW_SYSTEM, "评审 Agent 系统提示",
                AiConstants.PROMPT_CATEGORY_SYSTEM,
                "你是穿搭评审师。请检查执行说明是否符合用户的风格与场景要求，"
                        + "给出最终优化后的可交付版本。直接输出最终内容，不要多余解释。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_PLAN_USER_TEMPLATE, "规划用户消息模板",
                AiConstants.PROMPT_CATEGORY_USER_TEMPLATE, "用户需求：{0}"));
        baselineList.add(definePrompt(AiConstants.PROMPT_EXECUTE_USER_TEMPLATE, "执行用户消息模板",
                AiConstants.PROMPT_CATEGORY_USER_TEMPLATE, "规划方案：\n{0}"));
        baselineList.add(definePrompt(AiConstants.PROMPT_REVIEW_USER_TEMPLATE, "评审用户消息模板",
                AiConstants.PROMPT_CATEGORY_USER_TEMPLATE, "执行内容：\n{0}"));
        baselineList.add(definePrompt(AiConstants.PROMPT_DEFAULT_REQUIREMENT, "需求缺省值",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "生成一套日常穿搭"));
        baselineList.add(definePrompt(AiConstants.PROMPT_IMAGE_PERSON_DEFAULT, "人物图默认生成提示",
                AiConstants.PROMPT_CATEGORY_IMAGE,
                "一位穿着时尚的年轻人，全身照，真实摄影风格"));
        baselineList.add(definePrompt(AiConstants.PROMPT_IMAGE_PERSON_SUFFIX, "人物图提示风格后缀",
                AiConstants.PROMPT_CATEGORY_IMAGE, "，人物全身照，真实摄影风格"));
        baselineList.add(definePrompt(AiConstants.PROMPT_OUT_OF_SCOPE_REPLY, "超出范畴兜底话术",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY,
                "你问的问题超出范畴，我无法对你回答。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOAD_SKILL_TOOL_DESC, "技能加载工具描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC,
                "按技能名加载该技能 SKILL.md 的完整执行指令（正文）。"
                        + "当你根据技能目录判断用户请求命中某个技能后，必须先调用本工具（入参 skillName "
                        + "为技能目录中的名称），读到指令后再调用该技能的业务工具。无外网请求，直接从内存读取。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOAD_SKILL_ARG_DESC, "技能加载工具入参描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC,
                "技能名（技能目录中列出的 name），如 current-weather"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOAD_SKILL_MISSING_ARG, "技能加载工具缺参错误",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC,
                "load-skill-instructions 缺少必填参数 skillName"));
        baselineList.add(definePrompt(AiConstants.PROMPT_OUTFIT_SKILL_DESC, "穿搭技能描述",
                AiConstants.PROMPT_CATEGORY_SKILL_DESC,
                "根据上装、下装与场景，生成一套穿搭建议"));
        baselineList.add(definePrompt(AiConstants.PROMPT_OUTFIT_RESULT_TEMPLATE, "穿搭建议输出模板",
                AiConstants.PROMPT_CATEGORY_RESULT_TEMPLATE,
                "推荐搭配：{0} + {1}，适合「{2}」场景"));
        baselineList.add(definePrompt(AiConstants.PROMPT_OUTFIT_TOP_DEFAULT, "上装缺省值",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "未知上装"));
        baselineList.add(definePrompt(AiConstants.PROMPT_OUTFIT_PANTS_DEFAULT, "下装缺省值",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "未知下装"));
        baselineList.add(definePrompt(AiConstants.PROMPT_OUTFIT_SCENE_DEFAULT, "场景缺省值",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "日常"));
        baselineList.add(definePrompt(AiConstants.PROMPT_AUTH_LOGOUT_SUCCESS, "登出成功提示",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "已登出"));
        baselineList.add(definePrompt(AiConstants.PROMPT_AUTH_PASSWORD_RESET_SUCCESS, "密码重置成功提示",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "密码已重置，请使用新密码登录"));
        baselineList.add(definePrompt(AiConstants.PROMPT_ASSET_DELETE_SUCCESS, "素材删除成功提示",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "已删除"));
        baselineList.add(definePrompt(AiConstants.PROMPT_FAIL_ONCE_SUCCESS, "演示节点重试成功提示",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "第二次执行成功"));
        baselineList.add(definePrompt(AiConstants.PROMPT_TRYON_PENDING, "试穿任务排队中",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "任务排队中"));
        baselineList.add(definePrompt(AiConstants.PROMPT_TRYON_RUNNING, "试穿任务执行中",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "任务执行中"));
        baselineList.add(definePrompt(AiConstants.PROMPT_TRYON_SUCCEEDED, "试穿任务成功",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "成功"));
        baselineList.add(definePrompt(AiConstants.PROMPT_SKILL_GATE_BLOCKED, "技能懒加载闸门拦截提示",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC,
                "请先调用 load-skill-instructions（skillName={0}）加载技能指令，再重试 {1}"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOCATION_TOOL_DESC, "定位工具描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC,
                "按当前网络出口 IP 获取当前所在位置，返回城市名、纬度、经度。无参数。"
                        + "当用户询问当前位置天气但没有给出城市名时，先调用本工具，再用经纬度调用 get-weather。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOCATION_FAIL, "定位失败提示",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "定位失败: {0}"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOCATION_ERROR, "定位异常提示",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "定位异常: {0}"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOCATION_CITY_DEFAULT, "城市缺省值",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "未知"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_TOOL_DESC, "天气工具描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC,
                "查询指定位置的实时天气，返回天气现象、温度、相对湿度、风速。"
                        + "参数二选一：传 city 查指定城市（如 北京、上海）；"
                        + "或传 latitude 和 longitude 查经纬度所在位置（如 get-current-location 返回的坐标）。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_ARG_CITY_DESC, "天气工具 city 参数描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC, "城市名，如 北京、上海、Hangzhou"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_ARG_LAT_DESC, "天气工具 latitude 参数描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC, "纬度"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_ARG_LON_DESC, "天气工具 longitude 参数描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC, "经度"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_ARG_MISSING, "天气工具缺参错误",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY,
                "get-weather 需要提供 city，或 latitude+longitude，但调用中两者都为空"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_SOURCE_FAIL, "天气数据源全部不可用",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "两个开源天气数据源（Open-Meteo、wttr.in）均不可用"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_LOCATION_DEFAULT, "经纬度模式位置默认名",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "当前位置"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_RESULT_METEO, "Open-Meteo 天气结果模板",
                AiConstants.PROMPT_CATEGORY_RESULT_TEMPLATE,
                "【{0}实时天气】天气：{1}；温度：{2}℃；相对湿度：{3}%；风速：{4} km/h；观测时间：{5}"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_RESULT_WTTR, "wttr.in 天气结果模板",
                AiConstants.PROMPT_CATEGORY_RESULT_TEMPLATE,
                "【{0}实时天气】天气：{1}；温度：{2}℃；体感：{3}℃；相对湿度：{4}%；风速：{5} km/h"));
        baselineList.add(definePrompt(AiConstants.PROMPT_NEWS_TOOL_DESC, "科技新闻工具描述",
                AiConstants.PROMPT_CATEGORY_TOOL_DESC,
                "抓取今日最新科技新闻条目（标题、链接、来源、发布时间），无参数。"
                        + "当用户询问科技新闻、科技圈动态、最近有什么新消息时调用；"
                        + "返回原始新闻列表后，由你把外文标题翻译成中文并提炼 3-5 条要点，不要原样输出 JSON。"));
        baselineList.add(definePrompt(AiConstants.PROMPT_NEWS_SOURCE_FAIL, "新闻数据源全部不可用",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY,
                "三个免费新闻源（GDELT、Wikinews、Hacker News）均不可用"));
        baselineList.add(definePrompt(AiConstants.PROMPT_LOAD_SKILL_NOT_FOUND, "技能不存在错误提示",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "不存在名为 {0} 的技能"));
        baselineList.add(definePrompt(AiConstants.PROMPT_DEFAULT_USER_MESSAGE, "用户消息缺省值",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "你好"));
        baselineList.add(definePrompt(AiConstants.PROMPT_WEATHER_WMO_UNKNOWN, "WMO 代码未知兜底",
                AiConstants.PROMPT_CATEGORY_FIXED_REPLY, "未知（WMO code={0}）"));
        return baselineList;
    }

    /**
     * 组装一条提示词定义
     *
     * @param promptCode 提示词编码
     * @param promptName 展示名称
     * @param category   分类
     * @param content    提示词内容
     * @return 提示词定义
     */
    private static PromptDefinition definePrompt(String promptCode, String promptName,
                                                 String category, String content) {
        PromptDefinition promptDefinition = new PromptDefinition();
        promptDefinition.setPromptCode(promptCode);
        promptDefinition.setPromptName(promptName);
        promptDefinition.setCategory(category);
        promptDefinition.setContent(content);
        return promptDefinition;
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
