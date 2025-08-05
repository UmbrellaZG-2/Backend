# UmbrellaZG 网站后端系统

## 项目概述
一个基于Spring Boot的网站后端系统，提供了文章管理、用户认证、文件上传下载等功能。

## 技术栈
- **核心框架**: Spring Boot 3.4.5
- **Web框架**: Spring Web
- **数据访问**: Spring Data JPA
- **安全认证**: Spring Security + JWT
- **数据库**: MySQL
- **工具库**: 
  - Lombok (减少样板代码)
  - ModelMapper (对象映射)
  - Commons-IO (文件操作)
  - Flexmark (Markdown处理)
  - JJWT (JWT令牌生成与验证)
- **构建工具**: Maven
- **Java版本**: Java 17

## 中间件
- **数据库**: MySQL (已配置为默认数据库)
- **文件存储**: 本地文件系统 (通过FileStorageConfig配置)

## 主要功能
1. **用户认证与授权**
   - 管理员登录认证
   - 基于角色的权限控制
   - JWT令牌生成与验证

2. **文章管理**
   - 文章CRUD操作
   - 按分类查询文章
   - 分页查询功能

3. **文件管理**
   - 文章图片上传/下载/删除
   - 附件上传/下载/删除 (支持7Z、ZIP、RAR格式)

4. **访客留言**
   - 访客信息记录
   - 留言管理

## 项目结构
```
UmbrellaZG/
├── src/
│   ├── main/
│   │   ├── java/com/website/backend/
│   │   │   ├── config/         # 配置类
│   │   │   ├── controller/     # 控制器
│   │   │   ├── entity/         # 实体类
│   │   │   ├── exception/      # 异常类
│   │   │   ├── model/          # 模型类
│   │   │   ├── repository/     # 数据访问层
│   │   │   ├── security/       # 安全相关
│   │   │   ├── service/        # 服务接口
│   │   │   ├── service/impl/   # 服务实现
│   │   │   └── util/           # 工具类
│   │   └── resources/          # 资源文件
│   └── test/                   # 测试代码
├── pom.xml                     # Maven依赖
└── README.md                   # 项目说明
```

## 配置说明
主要配置文件位于`src/main/resources/application.properties`，包含以下关键配置：
- 服务器端口: `server.port=8080`
- 数据库配置: 使用MySQL数据库，配置信息在application.properties中
- JWT配置: `app.jwtSecret`和`app.jwtExpirationMs`

## 运行说明
1. 确保安装了Java 17和Maven
2. 克隆项目到本地
3. 进入项目根目录
4. 执行`mvn spring-boot:run`命令启动应用
5. 访问`http://localhost:8080`查看应用
6. MySQL数据库连接信息: 请参考application.properties文件中的配置

## 管理员登录
访问`/api/admin/login`接口进行登录