-- 迁移：执行模型重构——指针改 nodeType、上下文列改名、状态口径统一、节点表加业务备份列
-- 1) flow_task.current_node_index(int) -> current_node_type(varchar)：指针记"下一个要执行的节点类型"
-- 2) flow_task.context -> task_context：任务执行上下文（业务数据 + 节点输出，每次推进落库）
-- 3) 状态口径：任务 WAITING/RETRY_WAITING -> INIT；节点 WAIT_CALLBACK -> WAIT
-- 4) ext_info：业务数据 JSON 备份（flow_task 与 task_node 都有，框架只存不动）

ALTER TABLE flow_task
    CHANGE COLUMN current_node_index current_node_type VARCHAR(64) NOT NULL DEFAULT ''
        COMMENT '当前执行到的节点类型（指向下一个要执行的节点，空串=未开始/已走完）';

UPDATE flow_task SET current_node_type = '' WHERE current_node_type REGEXP '^[0-9]+$';

ALTER TABLE flow_task
    CHANGE COLUMN context task_context TEXT NULL
        COMMENT '任务执行上下文（业务数据 + 节点输出 JSON），每次推进落库，下次唤起直接从库里拿';

ALTER TABLE flow_task
    ADD COLUMN ext_info TEXT NULL COMMENT '业务数据 JSON 备份（业务方存取，框架只存不动）' AFTER task_context;

UPDATE flow_task SET status = 'INIT' WHERE status IN ('WAITING', 'RETRY_WAITING');

ALTER TABLE task_node
    ADD COLUMN ext_info TEXT NULL COMMENT '本节点业务数据 JSON 备份（业务方存取，框架只存不动）' AFTER fail_message;

UPDATE task_node SET status = 'WAIT' WHERE status = 'WAIT_CALLBACK';
