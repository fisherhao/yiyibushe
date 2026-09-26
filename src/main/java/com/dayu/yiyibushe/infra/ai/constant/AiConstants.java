package com.dayu.yiyibushe.infra.ai.constant;

/**
 * AI 能力平台统一常量：跨模块共用的状态、类型与文档标准集中维护于此，禁止散落到各业务类重复定义。
 * <p>
 * 厂商标识与通信协议不在本类维护，统一引用 {@code ProviderInfo} 枚举，保证单一事实源。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public final class AiConstants {

    private AiConstants() {
    }

    // ==================== 通用启停状态（多表共用） ====================

    /** 启用状态 */
    public static final String STATUS_ENABLED = "ENABLED";

    // ==================== 模型治理字段 ====================

    /** 发布状态：已发布 */
    public static final String PUBLISH_STATUS_PUBLISHED = "PUBLISHED";

    /** 运行状态：可用 */
    public static final String RUNTIME_STATUS_ACTIVE = "ACTIVE";

    /** 可见范围：公开 */
    public static final String VISIBILITY_PUBLIC = "PUBLIC";

    /** 定义来源：本地文件基线 */
    public static final String SOURCE_TYPE_LOCAL_FILE = "LOCAL_FILE";

    /** 初始版本号 */
    public static final int INITIAL_VERSION = 1;

    // ==================== 函数执行器 ====================

    /** 执行器类型：本地容器 Bean */
    public static final String EXECUTOR_TYPE_NATIVE = "NATIVE";

    // ==================== 插件安装 ====================

    /** 生效范围：工作空间 */
    public static final String SCOPE_TYPE_WORKSPACE = "WORKSPACE";

    /** 安装状态：已安装 */
    public static final String INSTALL_STATUS_INSTALLED = "INSTALLED";

    /** 默认工作空间业务ID */
    public static final long DEFAULT_WORKSPACE_ID = 1L;

    // ==================== 插件内容清单 ====================

    /** 内容项类型：技能 */
    public static final String ITEM_TYPE_SKILL = "SKILL";

    // ==================== Skill 文档标准（Agent Skills 开放标准） ====================

    /** Skill 文档文件名 */
    public static final String SKILL_FILE_NAME = "SKILL.md";

    /** classpath 下技能根目录 */
    public static final String SKILL_CLASSPATH_DIRECTORY = "skills";

    /** Skill 扫描路径模式：classpath 下每个技能一个目录 */
    public static final String SKILL_LOCATION_PATTERN = "classpath*:skills/*/SKILL.md";

    /** frontmatter 分隔标记 */
    public static final String FRONTMATTER_DELIMITER = "---";

    // ==================== 提示词库（ai_prompt） ====================

    /** 提示词分类：系统提示 */
    public static final String PROMPT_CATEGORY_SYSTEM = "SYSTEM";

    /** 提示词分类：用户消息模板 */
    public static final String PROMPT_CATEGORY_USER_TEMPLATE = "USER_TEMPLATE";

    /** 提示词分类：图片生成提示 */
    public static final String PROMPT_CATEGORY_IMAGE = "IMAGE_PROMPT";

    /** 提示词分类：固定话术 */
    public static final String PROMPT_CATEGORY_FIXED_REPLY = "FIXED_REPLY";

    /** 提示词分类：工具描述 */
    public static final String PROMPT_CATEGORY_TOOL_DESC = "TOOL_DESC";

    /** 提示词分类：技能描述 */
    public static final String PROMPT_CATEGORY_SKILL_DESC = "SKILL_DESC";

    /** 提示词分类：业务结果模板（技能/工具拼装输出） */
    public static final String PROMPT_CATEGORY_RESULT_TEMPLATE = "RESULT_TEMPLATE";

    /** 提示词状态：下线 */
    public static final String PROMPT_STATUS_INACTIVE = "INACTIVE";

    /** 助手 Agent 系统提示（技能目录段动态追加） */
    public static final String PROMPT_ASSISTANT_SYSTEM = "assistant-chat-system";

    /** 技能目录头部模板 */
    public static final String PROMPT_SKILL_METADATA_HEADER = "skill-metadata-header";

    /** 规划 Agent 系统提示 */
    public static final String PROMPT_PLAN_SYSTEM = "plan-agent-system";

    /** 执行 Agent 系统提示 */
    public static final String PROMPT_EXECUTE_SYSTEM = "execute-agent-system";

    /** 评审 Agent 系统提示 */
    public static final String PROMPT_REVIEW_SYSTEM = "review-agent-system";

    /** 规划用户消息模板（{0} 用户需求） */
    public static final String PROMPT_PLAN_USER_TEMPLATE = "plan-user-template";

    /** 执行用户消息模板（{0} 规划方案） */
    public static final String PROMPT_EXECUTE_USER_TEMPLATE = "execute-user-template";

    /** 评审用户消息模板（{0} 执行内容） */
    public static final String PROMPT_REVIEW_USER_TEMPLATE = "review-user-template";

    /** 需求缺省值 */
    public static final String PROMPT_DEFAULT_REQUIREMENT = "default-requirement";

    /** 无人物图时的默认文生图提示 */
    public static final String PROMPT_IMAGE_PERSON_DEFAULT = "image-person-default";

    /** 用户提示词追加的人物图风格后缀 */
    public static final String PROMPT_IMAGE_PERSON_SUFFIX = "image-person-suffix";

    /** 超出能力范畴的兜底话术 */
    public static final String PROMPT_OUT_OF_SCOPE_REPLY = "out-of-scope-reply";

    /** load-skill-instructions 工具描述 */
    public static final String PROMPT_LOAD_SKILL_TOOL_DESC = "load-skill-tool-desc";

    /** load-skill-instructions 入参 skillName 描述 */
    public static final String PROMPT_LOAD_SKILL_ARG_DESC = "load-skill-arg-desc";

    /** load-skill-instructions 缺少入参时的错误提示 */
    public static final String PROMPT_LOAD_SKILL_MISSING_ARG = "load-skill-missing-arg";

    // ==================== 穿搭技能（outfit-advice） ====================

    /** 穿搭技能描述 */
    public static final String PROMPT_OUTFIT_SKILL_DESC = "outfit-skill-desc";

    /** 穿搭建议输出模板（{0} 上装、{1} 下装、{2} 场景） */
    public static final String PROMPT_OUTFIT_RESULT_TEMPLATE = "outfit-result-template";

    /** 上装缺省值 */
    public static final String PROMPT_OUTFIT_TOP_DEFAULT = "outfit-top-default";

    /** 下装缺省值 */
    public static final String PROMPT_OUTFIT_PANTS_DEFAULT = "outfit-pants-default";

    /** 场景缺省值 */
    public static final String PROMPT_OUTFIT_SCENE_DEFAULT = "outfit-scene-default";

    // ==================== 通用成功话术 ====================

    /** 登出成功提示 */
    public static final String PROMPT_AUTH_LOGOUT_SUCCESS = "auth-logout-success";

    /** 密码重置成功提示 */
    public static final String PROMPT_AUTH_PASSWORD_RESET_SUCCESS = "auth-password-reset-success";

    /** 素材删除成功提示 */
    public static final String PROMPT_ASSET_DELETE_SUCCESS = "asset-delete-success";

    /** 演示节点重试成功提示 */
    public static final String PROMPT_FAIL_ONCE_SUCCESS = "fail-once-success";

    /** 试穿任务排队中 */
    public static final String PROMPT_TRYON_PENDING = "tryon-pending";

    /** 试穿任务执行中 */
    public static final String PROMPT_TRYON_RUNNING = "tryon-running";

    /** 试穿任务成功 */
    public static final String PROMPT_TRYON_SUCCEEDED = "tryon-succeeded";

    // ==================== 工具通用（所有业务工具共用） ====================

    /** 技能懒加载闸门拦截提示（{0} 技能名、{1} 工具名） */
    public static final String PROMPT_SKILL_GATE_BLOCKED = "skill-gate-blocked-message";

    // ==================== 定位工具（get-current-location） ====================

    /** 定位工具描述 */
    public static final String PROMPT_LOCATION_TOOL_DESC = "location-tool-desc";

    /** 定位失败提示（{0} 接口返回消息） */
    public static final String PROMPT_LOCATION_FAIL = "location-fail-message";

    /** 定位异常提示（{0} 异常消息） */
    public static final String PROMPT_LOCATION_ERROR = "location-error-message";

    /** 城市缺省值 */
    public static final String PROMPT_LOCATION_CITY_DEFAULT = "location-city-default";

    // ==================== 天气工具（get-weather） ====================

    /** 天气工具描述 */
    public static final String PROMPT_WEATHER_TOOL_DESC = "weather-tool-desc";

    /** 天气工具 city 参数描述 */
    public static final String PROMPT_WEATHER_ARG_CITY_DESC = "weather-arg-city-desc";

    /** 天气工具 latitude 参数描述 */
    public static final String PROMPT_WEATHER_ARG_LAT_DESC = "weather-arg-lat-desc";

    /** 天气工具 longitude 参数描述 */
    public static final String PROMPT_WEATHER_ARG_LON_DESC = "weather-arg-lon-desc";

    /** 天气工具缺参错误 */
    public static final String PROMPT_WEATHER_ARG_MISSING = "weather-arg-missing";

    /** 天气数据源全部不可用 */
    public static final String PROMPT_WEATHER_SOURCE_FAIL = "weather-source-fail";

    /** 经纬度模式位置默认名 */
    public static final String PROMPT_WEATHER_LOCATION_DEFAULT = "weather-location-default";

    /** Open-Meteo 天气结果模板（{0} 位置、{1} 天气、{2} 温度、{3} 湿度、{4} 风速、{5} 观测时间） */
    public static final String PROMPT_WEATHER_RESULT_METEO = "weather-result-meteo";

    /** wttr.in 天气结果模板（{0} 位置、{1} 天气、{2} 温度、{3} 体感、{4} 湿度、{5} 风速） */
    public static final String PROMPT_WEATHER_RESULT_WTTR = "weather-result-wttr";

    // ==================== 科技新闻工具（get-tech-news） ====================

    /** 科技新闻工具描述 */
    public static final String PROMPT_NEWS_TOOL_DESC = "news-tool-desc";

    /** 新闻数据源全部不可用 */
    public static final String PROMPT_NEWS_SOURCE_FAIL = "news-source-fail";

    // ==================== 技能加载工具（load-skill-instructions） ====================

    /** 技能不存在错误提示（{0} 技能名） */
    public static final String PROMPT_LOAD_SKILL_NOT_FOUND = "load-skill-not-found";

    // ==================== 对话兜底 ====================

    /** 用户消息缺省值（空消息时的默认输入） */
    public static final String PROMPT_DEFAULT_USER_MESSAGE = "default-user-message";

    // ==================== 天气 WMO 代码 ====================

    /** WMO 代码未知兜底（{0} 代码值） */
    public static final String PROMPT_WEATHER_WMO_UNKNOWN = "weather-wmo-unknown";
}
