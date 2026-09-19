-- 图片存储收敛为一张 asset 表（用户明确：情境双表没必要）：
--   1. 删除情境双表 image_scene / scene_image（V20260919_1720 创建，作废）
--   2. asset 表精简为用户要求的列：id、asset_id、url（本地路径，暂无 OSS）、
--      category（人物/帽子/上衣/裤子/袜子/鞋子）、user_id（归属人）、
--      gmt_expire（图片过期时间）、gmt_create、gmt_modify
--      删除多余列 object_key / file_name / file_size，新增过期时间列

DROP TABLE IF EXISTS scene_image;
DROP TABLE IF EXISTS image_scene;

ALTER TABLE asset
    ADD COLUMN gmt_expire DATETIME NOT NULL COMMENT '图片过期时间（上传时间 + 7 天，到期后列表不再展示）' AFTER url,
    DROP COLUMN object_key,
    DROP COLUMN file_name,
    DROP COLUMN file_size;
