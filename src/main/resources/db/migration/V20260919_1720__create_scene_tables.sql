-- 情境图片存储：图片按「情境」组织落库，支持"某个用户在某个情境下有哪些图片"的查询与展示。
-- 图片的访问链接统一落 asset 表（url 只存一份），本组表只做情境分组：
--   image_scene : 情境表——某用户创建了哪些情境（如：夏日海边 / 通勤穿搭）
--   scene_image : 情境图片关系表——某个情境下有哪些图片（关联 asset.asset_id）
-- 两表同库铁律：id 主键（sequence 连续发号）+ 业务 ID（18 位 IdUtil）+ gmt_create/gmt_modify。

CREATE TABLE IF NOT EXISTS image_scene (
    id         BIGINT       NOT NULL COMMENT '主键 ID（由 sequence 发号）',
    scene_id   BIGINT       NOT NULL COMMENT '业务情境 ID（18 位 IdUtil 发号，与主键 id 并存）',
    user_id    BIGINT       NOT NULL COMMENT '归属用户 ID（业务外键，关联 users.user_id）',
    scene_name VARCHAR(128) NOT NULL COMMENT '情境名称（同一用户内唯一，如：夏日海边）',
    gmt_create DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    gmt_modify DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_scene_id (scene_id),
    UNIQUE KEY uk_user_scene (user_id, scene_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图片情境表（某用户有哪些情境）';

CREATE TABLE IF NOT EXISTS scene_image (
    id         BIGINT   NOT NULL COMMENT '主键 ID（由 sequence 发号）',
    image_id   BIGINT   NOT NULL COMMENT '业务图片关系 ID（18 位 IdUtil 发号，与主键 id 并存）',
    scene_id   BIGINT   NOT NULL COMMENT '所属情境 ID（业务外键，关联 image_scene.scene_id）',
    asset_id   BIGINT   NOT NULL COMMENT '图片素材 ID（业务外键，关联 asset.asset_id，链接以 asset 表为准）',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    gmt_modify DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_image_id (image_id),
    UNIQUE KEY uk_scene_asset (scene_id, asset_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '情境图片关系表（某个情境下有哪些图片）';
