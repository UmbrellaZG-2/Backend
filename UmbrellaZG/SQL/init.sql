-- ========================================
-- UmbrellaZG 博客系统数据库初始化脚本
-- 创建时间: 2025-8-1 
-- 数据库版本: MySQL 8.0+
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `umbrellazg` 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `umbrellazg`;

-- ========================================
-- 1. 角色表 (roles)
-- ========================================
CREATE TABLE `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    PRIMARY KEY (`id`),
    INDEX `idx_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 插入默认角色
INSERT INTO `roles` (`name`) VALUES 
('ROLE_ADMIN'),
('ROLE_VISITOR');

-- ========================================
-- 2. 用户表 (users)
-- ========================================
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 3. 用户角色关联表 (user_roles)
-- ========================================
CREATE TABLE `user_roles` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ========================================
-- 4. 文章表 (articles)
-- ========================================
CREATE TABLE `articles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` BIGINT NOT NULL UNIQUE COMMENT '文章编号，用于业务标识',
    `title` VARCHAR(100) COMMENT '文章标题',
    `content` LONGTEXT COMMENT '文章内容',
    `category` VARCHAR(50) COMMENT '文章分类',
    `add_attach` BOOLEAN DEFAULT FALSE COMMENT '是否有附件',
    `add_picture` BOOLEAN DEFAULT FALSE COMMENT '是否有封面图片',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ========================================
-- 5. 标签表 (tags)
-- ========================================
CREATE TABLE `tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`name`),
    INDEX `idx_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ========================================
-- 6. 文章标签关联表 (article_tags)
-- ========================================
CREATE TABLE `article_tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE,
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- ========================================
-- 7. 评论表 (comments)
-- ========================================
CREATE TABLE `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` BIGINT NOT NULL COMMENT '所属文章ID',
    `parent_id` BIGINT NULL COMMENT '父评论ID（顶级评论为null）',
    `nickname` VARCHAR(100) NOT NULL COMMENT '昵称',
    `content` LONGTEXT NOT NULL COMMENT '评论内容',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `ip_address` VARCHAR(50) COMMENT '评论者IP地址',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ========================================
-- 8. 附件表 (attachments)
-- ========================================
CREATE TABLE `attachments` (
    `attachment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(255) NOT NULL COMMENT '文件路径',
    `file_type` VARCHAR(100) COMMENT '文件类型',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `upload_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `article_id` BIGINT NOT NULL COMMENT '所属文章ID',
    PRIMARY KEY (`attachment_id`),
    FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_file_type` (`file_type`),
    INDEX `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';

-- ========================================
-- 9. 文章图片表 (article_pictures)
-- ========================================
CREATE TABLE `article_pictures` (
    `picture_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(255) NOT NULL COMMENT '文件路径',
    `file_type` VARCHAR(100) COMMENT '文件类型',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `upload_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `article_id` BIGINT NOT NULL UNIQUE COMMENT '所属文章ID',
    PRIMARY KEY (`picture_id`),
    FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_article_id` (`article_id`),
    INDEX `idx_file_type` (`file_type`),
    INDEX `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章图片表';

-- ========================================
-- 10. 系统配置表 (tb_cfg_system)
-- ========================================
CREATE TABLE `tb_cfg_system` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_desc` VARCHAR(255) COMMENT '配置描述',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO `tb_cfg_system` (`config_key`, `config_value`, `config_desc`) VALUES 
('max_attachment_count', '5', '每篇文章最大附件数量'),
('max_file_size', '10485760', '单个文件最大大小（字节）'),
('allowed_file_types', '7z,zip,rar', '允许上传的文件类型'),
('guest_expiration_hours', '6', '游客登录过期时间（小时）');

-- ========================================
-- 创建默认管理员用户
-- 密码: admin123 (需要在实际使用时修改)
-- ========================================
INSERT INTO `users` (`username`, `password`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa');

-- 为管理员分配管理员角色
INSERT INTO `user_roles` (`user_id`, `role_id`) 
SELECT u.id, r.id 
FROM `users` u, `roles` r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- ========================================
-- 创建索引优化查询性能
-- ========================================

-- 文章表复合索引
CREATE INDEX `idx_articles_category_create_time` ON `articles` (`category`, `create_time` DESC);
CREATE INDEX `idx_articles_title_content` ON `articles` (`title`, `content`(100));

-- 评论表复合索引
CREATE INDEX `idx_comments_article_create_time` ON `comments` (`article_id`, `create_time` DESC);

-- 附件表复合索引
CREATE INDEX `idx_attachments_article_upload_time` ON `attachments` (`article_id`, `upload_time` DESC);

-- ========================================
-- 创建视图
-- ========================================

-- 文章统计视图
CREATE VIEW `v_article_stats` AS
SELECT 
    a.id,
    a.article_id,
    a.title,
    a.category,
    a.create_time,
    COUNT(DISTINCT c.id) as comment_count,
    COUNT(DISTINCT at.tag_id) as tag_count,
    CASE WHEN ap.picture_id IS NOT NULL THEN 1 ELSE 0 END as has_picture,
    CASE WHEN att.attachment_id IS NOT NULL THEN 1 ELSE 0 END as has_attachment
FROM `articles` a
LEFT JOIN `comments` c ON a.id = c.article_id
LEFT JOIN `article_tags` at ON a.id = at.article_id
LEFT JOIN `article_pictures` ap ON a.id = ap.article_id
LEFT JOIN `attachments` att ON a.id = att.article_id
GROUP BY a.id, a.article_id, a.title, a.category, a.create_time, ap.picture_id, att.attachment_id;

-- ========================================
-- 创建存储过程
-- ========================================

-- 清理孤立数据的存储过程
DELIMITER //
CREATE PROCEDURE `sp_cleanup_orphaned_data`()
BEGIN
    -- 清理孤立的文章标签关联
    DELETE at FROM `article_tags` at
    LEFT JOIN `articles` a ON at.article_id = a.id
    LEFT JOIN `tags` t ON at.tag_id = t.id
    WHERE a.id IS NULL OR t.id IS NULL;
    
    -- 清理孤立的评论
    DELETE c FROM `comments` c
    LEFT JOIN `articles` a ON c.article_id = a.id
    WHERE a.id IS NULL;
    
    -- 清理孤立的附件
    DELETE att FROM `attachments` att
    LEFT JOIN `articles` a ON att.article_id = a.id
    WHERE a.id IS NULL;
    
    -- 清理孤立的文章图片
    DELETE ap FROM `article_pictures` ap
    LEFT JOIN `articles` a ON ap.article_id = a.id
    WHERE a.id IS NULL;
    
    SELECT 'Cleanup completed' as result;
END //
DELIMITER ;

-- ========================================
-- 创建触发器
-- ========================================

-- 文章删除时自动清理相关数据
DELIMITER //
CREATE TRIGGER `tr_articles_before_delete` 
BEFORE DELETE ON `articles`
FOR EACH ROW
BEGIN
    -- 删除相关附件
    DELETE FROM `attachments` WHERE `article_id` = OLD.id;
    -- 删除相关图片
    DELETE FROM `article_pictures` WHERE `article_id` = OLD.id;
    -- 删除相关评论
    DELETE FROM `comments` WHERE `article_id` = OLD.id;
    -- 删除相关标签关联
    DELETE FROM `article_tags` WHERE `article_id` = OLD.id;
END //
DELIMITER ;

-- ========================================
-- 完成提示
-- ========================================
SELECT 'UmbrellaZG 数据库初始化完成！' as message;
