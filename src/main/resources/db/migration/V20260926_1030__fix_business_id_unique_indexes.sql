-- ============================================================
-- 修复业务ID唯一索引缺失
-- 规范：每张表 = 物理主键 id（AUTO_INCREMENT）+ 业务ID 唯一索引（IdUtil 18 位分段 ID）
-- 审计发现 3 张表业务ID无唯一约束：
--   users.user_id / flow_task.task_id / task_node.task_node_id
-- 说明：sequence 表业务键为 name（已有唯一索引），不在本次范围
-- ============================================================

-- 1. users.user_id 加唯一索引
ALTER TABLE users
    ADD UNIQUE KEY uk_user_id (user_id);

-- 2. flow_task.task_id 加唯一索引
ALTER TABLE flow_task
    ADD UNIQUE KEY uk_task_id (task_id);

-- 3. task_node.task_node_id 加唯一索引
ALTER TABLE task_node
    ADD UNIQUE KEY uk_task_node_id (task_node_id);
