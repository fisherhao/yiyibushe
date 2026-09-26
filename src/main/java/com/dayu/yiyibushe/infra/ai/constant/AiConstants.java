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
}
