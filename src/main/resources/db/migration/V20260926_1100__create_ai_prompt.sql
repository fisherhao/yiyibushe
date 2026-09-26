-- ============================================================
-- AI 提示词库：全项目所有提示词/文案模板的唯一存储
-- 设计要点：
--   1) prompt_code 业务编码（kebab-case），代码只认编码；
--   2) content 支持 {0} 编号占位符，由 PromptStore.format 渲染；
--   3) version 单调递增，PromptStore 定时按 version 增量拉取，改库后秒级生效、无需重启；
--   4) status：ACTIVE 生效 / INACTIVE 下线；
--   5) 时间列 gmt_create / gmt_modify，字符集 utf8mb4，引擎 InnoDB。
-- ============================================================

CREATE TABLE ai_prompt (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物理主键',
    prompt_code  VARCHAR(64)  NOT NULL COMMENT '提示词编码（kebab-case），代码引用',
    prompt_name  VARCHAR(128) NOT NULL COMMENT '展示名称',
    category     VARCHAR(32)  NOT NULL COMMENT '分类：SYSTEM/USER_TEMPLATE/IMAGE_PROMPT/FIXED_REPLY/TOOL_DESC',
    content      MEDIUMTEXT   NOT NULL COMMENT '提示词内容（支持 {0} 编号占位符）',
    status       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    version      INT          NOT NULL DEFAULT 1 COMMENT '版本号（增量拉取用）',
    remark       VARCHAR(256) COMMENT '备注',
    gmt_create   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modify   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_prompt_code (prompt_code),
    KEY idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词库';
