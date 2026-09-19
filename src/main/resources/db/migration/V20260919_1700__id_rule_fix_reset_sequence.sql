-- 纠正发号逻辑：主键 id 由 sequence 表连续发号（小数字），业务 ID（user_id / task_id /
-- task_node_id / asset_id）统一为 18 位（IdUtil：8 位混淆时间 + 10 位序号）。
-- 此前 id 与业务 ID 均由 sequence 发号、混在一起，且历史测试数据两列同为 2 万多，属脏数据。
-- 本脚本清空各业务表数据并重置序列，重新从 1 开始发号。

DELETE FROM users;
DELETE FROM flow_task;
DELETE FROM task_node;
DELETE FROM asset;

UPDATE sequence SET current_value = 0 WHERE name IN ('users', 'flow_task', 'task_node', 'asset');
