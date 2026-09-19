-- 任务类型不再单独成列：taskType 只存 task_context 一份（键 taskType），
-- 先把存量任务的 task_type 灌进 task_context JSON，再删除该列。
UPDATE flow_task
SET task_context = JSON_MERGE_PATCH(COALESCE(task_context, '{}'),
                                    JSON_OBJECT('taskType', task_type))
WHERE task_type IS NOT NULL;

ALTER TABLE flow_task DROP COLUMN task_type;
