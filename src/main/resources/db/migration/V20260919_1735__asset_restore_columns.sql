-- 恢复 asset 表的 object_key / file_name / file_size 三列（用户明确：都有用）：
--   object_key : 存储对象 key，未来对接 OSS 直接复用
--   file_name  : 图片原始文件名，展示用
--   file_size  : 文件大小，未来统计存储用量
-- gmt_expire（图片过期时间）保留。

ALTER TABLE asset
    ADD COLUMN object_key VARCHAR(512) NULL COMMENT '存储对象 key（本地相对路径或 OSS key）' AFTER url,
    ADD COLUMN file_name VARCHAR(255) NULL COMMENT '图片原始文件名' AFTER object_key,
    ADD COLUMN file_size BIGINT NULL COMMENT '文件大小（字节）' AFTER file_name;
