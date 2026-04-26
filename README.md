# Java 多客户端在线聊天系统

## 项目概述

这是一个基于 Java Socket 编程的多客户端在线聊天系统，包含服务端和客户端两个部分。系统支持公共聊天室和私人消息功能，并将聊天记录存储到 MySQL 数据库中。

## 技术栈

- **Java 16**
- **Socket 网络编程**
- **多线程**
- **Swing 图形界面**
- **JDBC + MySQL**

## 项目结构

```
chatroom/
├── server/              # 服务端代码
│   └── src/main/java/com/chat/server/
│       ├── Server.java        # 服务端主类
│       └── ClientHandler.java # 客户端处理器
├── client/              # 客户端代码
│   └── src/main/java/com/chat/client/
│       └── Client.java        # 客户端主类
├── common/              # 公共代码
│   └── src/main/java/com/chat/common/
│       ├── Message.java       # 消息类
│       └── DatabaseUtil.java  # 数据库工具类
├── resources/           # 资源文件
│   └── create_table.sql       # 数据库建表SQL
└── README.md            # 项目说明
```

## 功能特性

### 服务端
- 监听指定端口，支持多客户端并发连接
- 接收客户端消息，转发给所有在线客户端
- 聊天记录异步存入 MySQL，不阻塞主线程
- 控制台指令：查看在线客户端、关闭服务端
- 在线用户列表管理

### 客户端
- Swing 图形界面：聊天窗口、昵称设置、发送按钮、历史记录展示
- 连接服务端，自动加载历史聊天记录
- 发送消息，显示昵称 + 时间 + 内容
- 支持配置文件设置服务器 IP 与端口
- 公共聊天室和私人消息功能
- 双击用户列表打开独立的私聊窗口

## 数据库设计

### 库名：chat_db

### 表名：chat_history
| 字段名 | 数据类型 | 描述 |
|-------|---------|------|
| id    | INT     | 自增主键 |
| name  | VARCHAR(50) | 发送者昵称 |
| text  | TEXT    | 消息内容 |
| time  | BIGINT  | 时间戳（毫秒） |

## 快速开始

### 1. 环境准备
- JDK 8 或更高版本
- MySQL 5.7 或更高版本

### 2. 数据库初始化
1. 登录 MySQL 数据库
2. 执行 `resources/create_table.sql` 文件创建数据库和表：

```sql
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
```

### 3. 配置数据库连接
修改 `common/src/main/java/com/chat/common/DatabaseUtil.java` 文件中的数据库连接信息：

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_db?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "123456";
```

### 4. 编译项目
```bash
# 编译 common 模块
javac -d common/target/classes common/src/main/java/com/chat/common/*.java

# 编译服务端
javac -d server/target/classes -cp common/target/classes server/src/main/java/com/chat/server/*.java

# 编译客户端
javac -d client/target/classes -cp common/target/classes client/src/main/java/com/chat/client/*.java
```

### 5. 运行项目

#### 启动服务端
```bash
java -cp "server/target/classes;common/target/classes" com.chat.server.Server [port]
```

#### 启动客户端
```bash
java -cp "client/target/classes;common/target/classes" com.chat.client.Client
```

## 使用说明
1. **登录**：在客户端登录界面输入用户名，默认服务器地址为 localhost，端口为 8888
2. **公共聊天**：登录后，在公共聊天室区域输入消息，点击 "Send Public" 或按回车键发送
3. **私人消息**：双击左侧用户列表中的用户，在弹出的私聊窗口中发送消息
4. **服务端控制台命令**：
   - `list` - 查看在线用户
   - `stop` - 关闭服务端
   - `help` - 显示帮助信息

## 注意事项
- 确保 MySQL 服务已启动
- 确保数据库连接配置正确
- 服务端需要先启动，再启动客户端
- 客户端默认连接到 localhost:8888
- 支持中文用户名和消息

## 项目亮点
- 模块化设计，代码结构清晰
- 线程安全，支持多客户端并发
- 异步数据库操作，不阻塞主线程
- 友好的图形界面，支持独立的私聊窗口
- 完整的消息历史记录功能

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 联系方式

如果有任何问题，欢迎联系项目维护者。
