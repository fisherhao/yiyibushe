-- 发号器收敛为全库唯一序列（用户要求：sequence 表只留一个 common，业务序列全删）：
--   保留 name='sequence' 引导行（自依赖发号机制必需）与 name='common'（唯一发号序列）
--   删除按表名发号时代的所有业务序列行（users / flow_task / task_node / asset 等）
--   主键 id 与 18 位业务 ID 统一从 common 取号，历史序列号丢弃（无连续性要求）

DELETE FROM sequence
WHERE name NOT IN ('sequence', 'common');
