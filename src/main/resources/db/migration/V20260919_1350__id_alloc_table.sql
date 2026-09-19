-- ============================================================
-- IdUtil 号段 ID 生成器的号段表（独立于 sequence 表）
-- 表名：id_alloc
-- 每行对应一个业务标识（biz_tag），max_id 记录已分配的最大 ID，step 为号段步长
-- 采用「先 UPDATE max_id += step，再 SELECT 回新值」的原子取段方式
-- ============================================================
CREATE TABLE IF NOT EXISTS id_alloc (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键（本表内部用）',
    biz_tag     VARCHAR(64)  NOT NULL COMMENT '业务标识（唯一索引）',
    max_id      BIGINT       NOT NULL DEFAULT 0 COMMENT '当前已分配最大 ID',
    step        INT          NOT NULL DEFAULT 1000 COMMENT '号段步长',
    description VARCHAR(255)          DEFAULT NULL COMMENT '描述',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_tag (biz_tag)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'IdUtil 号段表';

-- 初始化默认业务 common：max_id 从 0 开始，首段为 [1, 1000]
INSERT INTO id_alloc (biz_tag, max_id, step, description)
VALUES ('common', 0, 1000, '默认业务')
ON DUPLICATE KEY UPDATE step = VALUES(step);
