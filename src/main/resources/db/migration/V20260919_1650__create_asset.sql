-- 图片素材表：文件存本地（local-resource/assets），访问链接与归属关系落库。
-- 绑定关系：user_id 绑定归属人，category 绑定衣物类别（AVATAR 人物图 / HAT 帽子 / TOP 上衣 /
-- PANTS 裤子 / SOCKS 袜子 / SHOES 鞋子），试穿链路按 user_id + category 从本表取图。
CREATE TABLE IF NOT EXISTS asset (
    id         BIGINT       NOT NULL COMMENT '主键 ID（由 sequence 发号）',
    asset_id   BIGINT       NOT NULL COMMENT '业务素材 ID（由 sequence 发号，与主键 id 并存）',
    user_id    BIGINT       NOT NULL COMMENT '归属用户 ID（业务外键，关联 users.user_id）',
    category   VARCHAR(32)  NOT NULL COMMENT '素材分类：AVATAR/TOP/PANTS/SOCKS/SHOES/HAT',
    url        VARCHAR(512) NOT NULL COMMENT '访问链接（本地为站点相对路径，云端为公网 URL）',
    object_key VARCHAR(256) NOT NULL COMMENT '存储对象 key（本地相对路径或 OSS key）',
    file_name  VARCHAR(256)          DEFAULT NULL COMMENT '原始文件名',
    file_size  BIGINT                DEFAULT NULL COMMENT '文件大小（字节）',
    gmt_create DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（数据库默认填充）',
    gmt_modify DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（更新时数据库自动刷新）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_asset_id (asset_id),
    KEY idx_user_category (user_id, category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图片素材表（文件存本地、链接落库；user_id 绑人、category 绑衣物类别）';
