-- 重试次数是任务级概念（记在 flow_task.retry_count），task_node 不存重试字段，删除该列。
ALTER TABLE task_node DROP COLUMN retry_count;
