-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS chat_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE chat_db;

-- 创建聊天记录表
CREATE TABLE IF NOT EXISTS chat_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    text TEXT NOT NULL,
    time BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试数据
INSERT INTO chat_history (name, text, time) VALUES
('Server', 'Welcome to Java Multi-Client Chat System!', UNIX_TIMESTAMP() * 1000),
('Admin', 'This is a test message', UNIX_TIMESTAMP() * 1000);

-- 查看创建的表
SHOW TABLES;

-- 查看表结构
DESCRIBE chat_history;
