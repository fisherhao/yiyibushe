-- ============================================================
-- sequence 发号表补齐无语义主键 id（全库建表铁律：每张表都要有 id + gmt_create + gmt_modify）
--   1) name 保留为业务唯一标识（唯一键 uk_name），但不再是主键；
--   2) 新增 id 作主键：BIGINT UNSIGNED、无语义、由发号器生成、非自增；
--   3) 已有行按 name 顺序补 1..N 的小 id；
--   4) 新增 name = 'sequence' 的"引导行"：它专门给 sequence 表自身发号——
--      注册一个新序列时，新行的主键 id 从引导行取号（current_value 从 1000 起，
--      新序列 id 自 1001 开始，不会与上面的小 id 冲突），从而避免"发号器给自己发号"的死循环。
-- ============================================================

ALTER TABLE `sequence` DROP PRIMARY KEY;

ALTER TABLE `sequence` ADD COLUMN id BIGINT UNSIGNED NULL COMMENT '主键 ID（无业务语义，由发号器生成）' FIRST;

ALTER TABLE `sequence` ADD UNIQUE KEY uk_name (name);

-- 已有行补 id：窗口函数派生表保证每行一个唯一序号（序号只要求唯一，不要求与 name 对应）
UPDATE `sequence` AS target
JOIN (SELECT name, ROW_NUMBER() OVER (ORDER BY name) AS row_no FROM `sequence`) AS numbered
    ON numbered.name = target.name
SET target.id = numbered.row_no
WHERE target.id IS NULL;

ALTER TABLE `sequence` MODIFY COLUMN id BIGINT UNSIGNED NOT NULL COMMENT '主键 ID（无业务语义，由发号器生成）';

ALTER TABLE `sequence` ADD PRIMARY KEY (id);

-- 引导行：给 sequence 表自身发号
INSERT INTO `sequence` (id, name, current_value, increment_step)
VALUES (1000, 'sequence', 1000, 1)
ON DUPLICATE KEY UPDATE id = 1000, current_value = GREATEST(current_value, 1000), increment_step = 1;
