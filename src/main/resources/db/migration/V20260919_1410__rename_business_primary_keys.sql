-- ============================================================
-- 业务表主键列语义化：每张业务表主键列从通用 id 改为「业务语义名 + 下划线」
--   users     : id    -> user_id
--   flow_task : id    -> task_id
--   task_node : id    -> task_node_id
--
-- 说明：
--   1) V20260919_1214 已建表且字段为通用 id，本脚本用 RENAME COLUMN 改名（不改类型/默认值）；
--   2) 主键/PK 索引随列改名自动跟随；
--   3) 全库仍遵循：主键由发号器生成、create_time 数据库默认、update_time 自动刷新。
-- ============================================================
ALTER TABLE users     RENAME COLUMN id TO user_id;
ALTER TABLE flow_task RENAME COLUMN id TO task_id;
ALTER TABLE task_node RENAME COLUMN id TO task_node_id;
