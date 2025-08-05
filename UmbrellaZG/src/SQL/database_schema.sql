-- 数据库表结构设计

-- 创建文章表
CREATE TABLE articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL UNIQUE,
    title VARCHAR(100),
    content TEXT,
    category VARCHAR(100),
    add_attach BOOLEAN DEFAULT FALSE,
    add_picture BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 创建文章图片表
CREATE TABLE article_pictures (
    picture_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    upload_time DATETIME NOT NULL,
    article_id BIGINT NOT NULL,
    FOREIGN KEY (article_id) REFERENCES articles(article_id)
);

-- 创建附件表
CREATE TABLE attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    upload_time DATETIME NOT NULL,
    article_id BIGINT NOT NULL,
    FOREIGN KEY (article_id) REFERENCES articles(article_id)
);

-- 创建角色表
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- 创建用户角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 创建评论表
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    nickname VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL,
    ip_address VARCHAR(50),
    FOREIGN KEY (article_id) REFERENCES articles(article_id),
    FOREIGN KEY (parent_id) REFERENCES comments(id)
);

-- 创建标签表
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    create_time DATETIME NOT NULL
);

-- 创建文章-标签关联表
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES articles(article_id),
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);

-- 插入初始角色数据
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_VISITOR');