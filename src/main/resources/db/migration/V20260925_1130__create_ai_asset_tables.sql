-- ============================================================
-- AI 能力平台基建：7 张核心表 + 1 张凭证表
-- 建表规范：
--   1) 物理主键 id：BIGINT AUTO_INCREMENT，无业务语义；
--   2) 业务 ID（model_id 等）：BIGINT，由 IdUtil 发 18 位 ID，跨表引用走业务 ID；
--   3) 时间列：DATETIME，命名 gmt_create / gmt_modify；
--   4) 字符集 utf8mb4，引擎 InnoDB，不建物理外键；
--   5) 状态双层：publish_status（DRAFT/PUBLISHED/OFFLINE/DEPRECATED）
--      runtime_status（INIT/ACTIVE/FAILED/UNLOADED）
-- ============================================================

-- 1. AI 模型资源表
CREATE TABLE ai_model (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    model_id           BIGINT       NOT NULL COMMENT '业务ID（IdUtil）',
    model_code         VARCHAR(64)  NOT NULL COMMENT '模型编码，Agent 引用',
    display_name       VARCHAR(128) NOT NULL COMMENT '展示名称',
    provider           VARCHAR(64)  NOT NULL COMMENT '厂商标识，对应 CredentialManager 凭证 key',
    model_type         VARCHAR(32)  NOT NULL COMMENT 'TEXT_CHAT/IMAGE_GENERATION/IMAGE_EDITING/MULTIMODAL/EMBEDDING',
    protocol           VARCHAR(32)  NOT NULL COMMENT '接入协议：OPENAI_COMPATIBLE/DASHSCOPE_NATIVE/MCP(预留)/CUSTOM(预留)',
    base_url           VARCHAR(256) NOT NULL COMMENT 'API 基础地址',
    chat_path          VARCHAR(128) COMMENT '对话接口路径（OPENAI_COMPATIBLE 生效）',
    vendor_model_name  VARCHAR(128) NOT NULL COMMENT '厂商侧模型名',
    async_flag         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否异步任务模型',
    poll_interval_ms   BIGINT       NOT NULL DEFAULT 3000 COMMENT '异步轮询间隔',
    poll_timeout_ms    BIGINT       NOT NULL DEFAULT 180000 COMMENT '异步轮询超时',
    context_window     INT          COMMENT '上下文窗口（token）',
    max_output_tokens  INT          COMMENT '最大输出 token',
    input_modalities   JSON         COMMENT '输入模态，如 ["text"] / ["text","image"]',
    output_modalities  JSON         COMMENT '输出模态',
    supported_params   JSON         COMMENT '可调参数结构，如 {"temperature":{"type":"number","min":0,"max":2}}',
    default_params     JSON         COMMENT '默认推理参数，如 {"temperature":0.7}',
    icon               VARCHAR(256),
    category           VARCHAR(64)  COMMENT '分类',
    tags               VARCHAR(256) COMMENT '标签（逗号分隔）',
    publish_status     VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
    runtime_status     VARCHAR(16)  NOT NULL DEFAULT 'INIT' COMMENT 'INIT/ACTIVE/FAILED/UNLOADED',
    visibility         VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE' COMMENT 'PRIVATE/TEAM/PUBLIC',
    source_type        VARCHAR(16)  NOT NULL DEFAULT 'DB' COMMENT 'DB/LOCAL_FILE/MARKET/GIT',
    source_ref         VARCHAR(256),
    owner              VARCHAR(64),
    version            INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    remark             VARCHAR(256),
    gmt_create         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_model_id (model_id),
    UNIQUE KEY uk_model_code_version (model_code, version),
    KEY idx_provider (provider),
    KEY idx_publish_runtime (publish_status, runtime_status),
    FULLTEXT KEY ft_model (display_name, vendor_model_name) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型资源表';

-- 2. AI 函数执行体表
CREATE TABLE ai_function (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    function_id     BIGINT       NOT NULL COMMENT '业务ID',
    function_code   VARCHAR(64)  NOT NULL COMMENT '函数编码',
    function_name   VARCHAR(128) NOT NULL COMMENT '函数名称',
    description     VARCHAR(512) COMMENT '函数说明',
    executor_type   VARCHAR(16)  NOT NULL COMMENT 'NATIVE/HTTP/MCP(预留)/SCRIPT(预留)',
    executor_config JSON         NOT NULL COMMENT '执行配置：NATIVE={"bean":"...","method":"..."}；HTTP={"url":"...","method":"GET","authRef":"..."}；MCP={"serverCode":"...","toolName":"..."}',
    output_schema   JSON         COMMENT '输出 JSON Schema',
    status          VARCHAR(16)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    version         INT          NOT NULL DEFAULT 1,
    remark          VARCHAR(256),
    gmt_create      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_function_id (function_id),
    UNIQUE KEY uk_function_code (function_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 函数执行体表';

-- 3. AI 工具表（函数的模型视图）
CREATE TABLE ai_tool (
    id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    tool_id             BIGINT        NOT NULL COMMENT '业务ID',
    tool_code           VARCHAR(64)   NOT NULL COMMENT '工具编码（模型可见，kebab-case）',
    function_id         BIGINT        NOT NULL COMMENT '绑定 ai_function.function_id',
    display_name        VARCHAR(128)  NOT NULL COMMENT '展示名称',
    description         VARCHAR(1024) NOT NULL COMMENT '调用路由描述，决定模型命中率',
    input_schema        JSON          NOT NULL COMMENT '入参 JSON Schema 2020-12',
    provides            JSON          COMMENT '产出声明，如 ["weather_text"]（元数据，不做自动推导）',
    requires            JSON          COMMENT '入参依赖声明，如 ["city"]',
    discover_keywords   VARCHAR(512)  COMMENT '检索同义词/口语说法（逗号分隔）',
    category            VARCHAR(64)   COMMENT '分类',
    tags                VARCHAR(256)  COMMENT '标签（逗号分隔）',
    usage_count         BIGINT        NOT NULL DEFAULT 0 COMMENT '累计调用次数',
    concurrency_safe    TINYINT       NOT NULL DEFAULT 1 COMMENT '是否可并行调用',
    read_only           TINYINT       NOT NULL DEFAULT 1 COMMENT '是否只读',
    external_execution  TINYINT       NOT NULL DEFAULT 0 COMMENT '外部执行，触发人工确认闸门',
    destructive_hint    TINYINT       NOT NULL DEFAULT 0 COMMENT '破坏性操作提示',
    idempotent_hint     TINYINT       NOT NULL DEFAULT 0 COMMENT '幂等提示',
    timeout_seconds     INT           NOT NULL DEFAULT 8 COMMENT '超时秒数',
    retry_count         INT           NOT NULL DEFAULT 1 COMMENT '重试次数',
    cache_ttl_seconds   INT           NOT NULL DEFAULT 0 COMMENT '结果缓存秒数（0 不缓存）',
    icon                VARCHAR(256),
    publish_status      VARCHAR(16)   NOT NULL DEFAULT 'DRAFT',
    runtime_status      VARCHAR(16)   NOT NULL DEFAULT 'INIT',
    version             INT           NOT NULL DEFAULT 1,
    remark              VARCHAR(256),
    gmt_create          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tool_id (tool_id),
    UNIQUE KEY uk_tool_code_version (tool_code, version),
    KEY idx_function_id (function_id),
    KEY idx_publish_runtime (publish_status, runtime_status),
    KEY idx_category (category),
    FULLTEXT KEY ft_tool (display_name, description) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 工具表';

-- 4. AI 技能表（业务 SOP）
CREATE TABLE ai_skill (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    skill_id         BIGINT        NOT NULL COMMENT '业务ID',
    skill_code       VARCHAR(64)   NOT NULL COMMENT '技能编码',
    display_name     VARCHAR(128)  NOT NULL COMMENT '展示名称',
    description      VARCHAR(1024) NOT NULL COMMENT '触发条件描述（检索与候选决策依据）',
    instructions     MEDIUMTEXT    COMMENT 'SOP 正文（命中后懒加载）',
    tools            JSON          COMMENT '工具引用：[{"toolId":...,"required":true,"order":1,"presetConfig":{}}]',
    trigger_examples TEXT          COMMENT '触发/不触发 few-shot 注释',
    category         VARCHAR(64)   COMMENT '分类',
    tags             VARCHAR(256)  COMMENT '标签（逗号分隔）',
    usage_count      BIGINT        NOT NULL DEFAULT 0,
    license          VARCHAR(64)   DEFAULT 'Apache-2.0',
    compatibility    VARCHAR(256)  COMMENT '运行依赖说明',
    metadata_json    JSON          COMMENT '扩展元数据',
    sort_no          INT           NOT NULL DEFAULT 0,
    publish_status   VARCHAR(16)   NOT NULL DEFAULT 'DRAFT',
    runtime_status   VARCHAR(16)   NOT NULL DEFAULT 'INIT',
    version          INT           NOT NULL DEFAULT 1,
    remark           VARCHAR(256),
    gmt_create       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_skill_id (skill_id),
    UNIQUE KEY uk_skill_code_version (skill_code, version),
    FULLTEXT KEY ft_skill (display_name, description) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 技能表';

-- 5. AI 插件表（分发/安装单元）
CREATE TABLE ai_plugin (
    id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    plugin_id           BIGINT        NOT NULL COMMENT '业务ID',
    plugin_code         VARCHAR(64)   NOT NULL COMMENT '插件编码',
    display_name        VARCHAR(128)  NOT NULL COMMENT '展示名称',
    description         VARCHAR(1024) NOT NULL COMMENT '能力描述（市场展示与安装路由）',
    icon                VARCHAR(256),
    version             VARCHAR(16)   NOT NULL COMMENT '语义化版本',
    author              VARCHAR(64),
    owner               VARCHAR(64),
    visibility          VARCHAR(16)   NOT NULL DEFAULT 'PRIVATE' COMMENT 'PRIVATE/TEAM/PUBLIC',
    source_type         VARCHAR(16)   NOT NULL DEFAULT 'DB' COMMENT 'DB/LOCAL_FILE/MARKET/GIT',
    source_ref          VARCHAR(256),
    category            VARCHAR(64)   COMMENT '分类',
    tags                VARCHAR(256)  COMMENT '标签（逗号分隔）',
    items               JSON          COMMENT '内容清单：[{"itemType":"SKILL","itemId":...,"required":true}]',
    permission_config   JSON          COMMENT '权限声明（网络/存储/危险操作），执行器侧另有拦截点',
    credential_required TINYINT       NOT NULL DEFAULT 0 COMMENT '是否需要配置凭证',
    publish_status      VARCHAR(16)   NOT NULL DEFAULT 'DRAFT',
    runtime_status      VARCHAR(16)   NOT NULL DEFAULT 'INIT',
    remark              VARCHAR(256),
    gmt_create          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_id (plugin_id),
    UNIQUE KEY uk_plugin_code_version (plugin_code, version),
    FULLTEXT KEY ft_plugin (display_name, description) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 插件表';

-- 6. AI 插件安装实例表（空间级，凭证挂此层）
CREATE TABLE ai_plugin_install (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    install_id     BIGINT       NOT NULL COMMENT '业务ID',
    plugin_id      BIGINT       NOT NULL COMMENT '指向具体版本 ai_plugin.plugin_id',
    plugin_code    VARCHAR(64)  NOT NULL COMMENT '插件编码（冗余，便于检索）',
    version        VARCHAR(16)  NOT NULL COMMENT '所安装版本',
    scope_type     VARCHAR(16)  NOT NULL COMMENT '安装范围：WORKSPACE/USER',
    scope_id       BIGINT       NOT NULL COMMENT '空间或用户业务ID',
    credential_ref VARCHAR(64)  COMMENT '凭证引用（CredentialManager provider 标识），可空',
    config_json    JSON         COMMENT '安装级配置',
    install_status VARCHAR(16)  NOT NULL DEFAULT 'INSTALLED' COMMENT 'INSTALLED/UPGRADING/UNINSTALLED/FAILED',
    gmt_create     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_install_id (install_id),
    KEY idx_scope (scope_type, scope_id),
    KEY idx_plugin (plugin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 插件安装实例表';

-- 7. AI 能力挂载表（安装实例 → Agent/流程节点）
CREATE TABLE ai_target_mount (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    mount_id      BIGINT       NOT NULL COMMENT '业务ID',
    target_type   VARCHAR(16)  NOT NULL COMMENT '挂载对象类型：AGENT/FLOW_NODE',
    target_id     BIGINT       NOT NULL COMMENT '挂载对象业务ID',
    install_id    BIGINT       NOT NULL COMMENT 'ai_plugin_install.install_id',
    mount_type    VARCHAR(16)  NOT NULL COMMENT '挂载能力类型：PLUGIN/SKILL/TOOL',
    mount_ref_id  BIGINT       NOT NULL COMMENT 'plugin_id/skill_id/tool_id',
    preset_config JSON         COMMENT '预设参数',
    sort_no       INT          NOT NULL DEFAULT 0,
    status        VARCHAR(16)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    gmt_create    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mount_id (mount_id),
    UNIQUE KEY uk_target_mount (target_type, target_id, mount_type, mount_ref_id),
    KEY idx_install (install_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 能力挂载表';

-- 8. AI 凭证表（厂商密钥数据库存储，CredentialManager 的 DB 实现使用）
CREATE TABLE ai_credential (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    credential_id BIGINT       NOT NULL COMMENT '业务ID',
    provider      VARCHAR(64)  NOT NULL COMMENT '厂商标识：dashscope/qwen/moonshot/hunyuan 等',
    app_key       VARCHAR(256) COMMENT '应用标识（可空）',
    app_secret    VARCHAR(512) NOT NULL COMMENT '应用密钥',
    status        VARCHAR(16)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    remark        VARCHAR(256),
    gmt_create    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_credential_id (credential_id),
    UNIQUE KEY uk_provider (provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 凭证表';
