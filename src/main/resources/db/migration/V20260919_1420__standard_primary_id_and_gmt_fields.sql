-- ============================================================
-- 全库建表规范统一（用户第10轮铁律，本地规则已存档）：
--   1) 每张表都必须有主键 ID `id`：无业务语义、无符号 BIGINT（约 20 位），由 sequence 发号生成；
--   2) 业务 ID（user_id / task_id / node_id 等）是普通业务列，与主键 id 并存、互不冲突；
--   3) 每张表都必须有 `gmt_create`（数据库默认填充）与 `gmt_modify`（更新时数据库自动刷新）；
--   4) 所有时间字段一律以 GMT 开头（不接受 create_time / update_time 等其他命名）；
--   5) 该用下划线分隔的字段一律下划线分隔，不许粘成一个单词。
-- 说明：上一版 V20260919_1410 曾把主键从 id 改名成 user_id / task_id / task_node_id，
--       本脚本据此再补回无意义主键 id，并保留业务列并存、统一 GMT 时间命名、删掉冗余列。
-- ============================================================

-- ========== sequence 发号表：本表主键为业务标识 name（元数据），补 gmt 时间名 ==========
ALTER TABLE sequence CHANGE COLUMN create_time gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）';
ALTER TABLE sequence CHANGE COLUMN update_time gmt_modify DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时自动刷新）';

-- ========== users：放弃 user_id 作主键，补回无意义主键 id；user_id 保留为业务列 ==========
ALTER TABLE users DROP PRIMARY KEY;
ALTER TABLE users ADD COLUMN id BIGINT UNSIGNED NOT NULL COMMENT '主键 ID（无业务语义，sequence 发号）' AFTER user_id;
UPDATE users SET id = user_id;
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE users CHANGE COLUMN user_id user_id BIGINT NOT NULL COMMENT '业务用户 ID（与主键 id 并存）';
ALTER TABLE users CHANGE COLUMN create_time gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）';
ALTER TABLE users CHANGE COLUMN update_time gmt_modify DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时自动刷新）';

-- ========== flow_task：放弃 task_id 作主键，补回无意义主键 id；task_id 保留为业务列 ==========
ALTER TABLE flow_task DROP PRIMARY KEY;
ALTER TABLE flow_task ADD COLUMN id BIGINT UNSIGNED NOT NULL COMMENT '主键 ID（无业务语义，sequence 发号）' AFTER task_id;
UPDATE flow_task SET id = task_id;
ALTER TABLE flow_task ADD PRIMARY KEY (id);
ALTER TABLE flow_task CHANGE COLUMN task_id task_id BIGINT NOT NULL COMMENT '业务任务 ID（与主键 id 并存）';
ALTER TABLE flow_task CHANGE COLUMN create_time gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）';
ALTER TABLE flow_task CHANGE COLUMN update_time gmt_modify DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时自动刷新）';

-- ========== task_node：放弃 task_node_id 作主键，补回无意义主键 id；task_id/node_id 保留为业务列；删 wait_since_millis（执行时间由 flow_task 的 gmt_fire 决定）==========
ALTER TABLE task_node DROP PRIMARY KEY;
ALTER TABLE task_node DROP COLUMN wait_since_millis;
ALTER TABLE task_node ADD COLUMN id BIGINT UNSIGNED NOT NULL COMMENT '主键 ID（无业务语义，sequence 发号）' AFTER task_node_id;
UPDATE task_node SET id = task_node_id;
ALTER TABLE task_node ADD PRIMARY KEY (id);
ALTER TABLE task_node CHANGE COLUMN task_node_id task_node_id BIGINT NOT NULL COMMENT '业务节点记录 ID（与主键 id 并存）';
ALTER TABLE task_node CHANGE COLUMN create_time gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）';
ALTER TABLE task_node CHANGE COLUMN update_time gmt_modify DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时自动刷新）';
