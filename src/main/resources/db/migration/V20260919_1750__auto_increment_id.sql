-- 主键 id 改为数据库自增（用户决定推翻此前"非自增、由 sequence 发号"的规范）：
--   业务表（users / flow_task / task_node / asset）与 sequence 表的主键 id
--   全部改为 AUTO_INCREMENT，insert 时不再由应用层发号；
--   业务 ID（user_id / task_id / task_node_id / asset_id）仍由 IdUtil 发 18 位分段 ID；
--   sequence 表只保留 common 行（引导行 name='sequence' 随之作废删除）

ALTER TABLE users     MODIFY id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID（数据库自增）';
ALTER TABLE flow_task MODIFY id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID（数据库自增）';
ALTER TABLE task_node MODIFY id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID（数据库自增）';
ALTER TABLE asset     MODIFY id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID（数据库自增）';
ALTER TABLE sequence  MODIFY id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID（数据库自增）';

DELETE FROM sequence WHERE name = 'sequence';
