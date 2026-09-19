-- ============================================================
-- yiyibushe 初始化建表脚本（Flyway 首个版本）
-- 包含：sequence 发号序列表、users 用户表、flow_task 任务主表、task_node 节点执行记录表
--
-- 全库统一规范：
--   1) 主键 id 为 BIGINT 非自增，统一由 SequenceIdGenerator（sequence 表）按表名发号；
--   2) create_time 由数据库默认时间填充（DEFAULT CURRENT_TIMESTAMP），代码不传值；
--   3) update_time 更新时由数据库自动刷新（ON UPDATE CURRENT_TIMESTAMP），代码不传值。
--
-- 全部使用 IF NOT EXISTS，脚本可重复执行
-- ============================================================

-- 发号序列表：每个业务表一行，name = 表名，发号即 current_value + increment_step
-- 采用 INSERT ... ON DUPLICATE KEY UPDATE current_value = LAST_INSERT_ID(current_value + 步长)
-- 原子取号，LAST_INSERT_ID() 为连接级变量，同事务内可安全取回
CREATE TABLE IF NOT EXISTS `sequence` (
    name           VARCHAR(64) NOT NULL COMMENT '业务标识（约定为表名）',
    current_value  BIGINT      NOT NULL DEFAULT 0 COMMENT '当前已发号的最大值',
    increment_step INT         NOT NULL DEFAULT 1 COMMENT '步长（每次取号递增量）',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'ID 发号序列表';

-- 用户表（id 由 sequence 表按表名 users 发号，非自增）
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL COMMENT '主键 ID（由 sequence 发号）',
    username    VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    password    VARCHAR(128) NOT NULL COMMENT '密码摘要',
    nickname    VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    status      INT                   DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 任务主表：一个任务对应一条有序节点链
-- gmt_fire 为期望调度时间（毫秒时间戳），调度排序专用；时间规范字段为 create_time/update_time
CREATE TABLE IF NOT EXISTS flow_task (
    id                  BIGINT      NOT NULL COMMENT '任务 ID（由 sequence 发号）',
    task_type           VARCHAR(64) NOT NULL COMMENT '任务类型（对应节点链绑定名）',
    status              VARCHAR(32) NOT NULL COMMENT '任务状态：WAITING/RUNNING/RETRY_WAITING/SUCCESS/FAILED',
    priority            INT         NOT NULL DEFAULT 0 COMMENT '优先级：数值越小越优先',
    gmt_fire            BIGINT      NOT NULL COMMENT '期望调度时间（毫秒时间戳）',
    retry_count         INT         NOT NULL DEFAULT 0 COMMENT '已重试次数',
    max_retry           INT         NOT NULL DEFAULT 0 COMMENT '最大重试次数',
    retry_strategy_code VARCHAR(32)          DEFAULT NULL COMMENT '重试间隔策略 code：FIXED/FIBONACCI/SEQUENCE，空走全局默认',
    current_node_index  INT         NOT NULL DEFAULT 0 COMMENT '当前执行到的节点下标（断点续跑依据）',
    context             JSON                 DEFAULT NULL COMMENT '任务级业务数据',
    create_time         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    update_time         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (id),
    KEY idx_dispatch (status, gmt_fire, priority)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'FlowTask 任务主表';

-- 节点执行记录表：任务每经过一个节点在此落一条执行记录
CREATE TABLE IF NOT EXISTS task_node (
    id                BIGINT       NOT NULL COMMENT '记录 ID（由 sequence 发号）',
    task_id           BIGINT       NOT NULL COMMENT '所属任务 ID',
    node_id           VARCHAR(64)  NOT NULL COMMENT '节点 ID',
    node_order        INT          NOT NULL COMMENT '节点在链上的顺序（从 0 开始）',
    status            VARCHAR(32)  NOT NULL COMMENT '节点状态：INIT/PROCESSING/WAIT_CALLBACK/SUCCESS/FAILED',
    retry_count       INT          NOT NULL DEFAULT 0 COMMENT '本节点已重试次数',
    output            JSON                  DEFAULT NULL COMMENT '节点输出',
    fail_message      VARCHAR(512)          DEFAULT NULL COMMENT '失败原因',
    wait_since_millis BIGINT       NOT NULL DEFAULT 0 COMMENT '进入待回调状态的时间戳（毫秒），0 表示未在等待',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'FlowTask 节点执行记录表';

-- sequence 初始化：预置各业务表的起始行（current_value 从 0 开始，首个 ID 为 步长）
INSERT INTO `sequence` (name, current_value, increment_step)
VALUES ('users', 0, 1), ('flow_task', 0, 1), ('task_node', 0, 1)
ON DUPLICATE KEY UPDATE current_value = current_value;
