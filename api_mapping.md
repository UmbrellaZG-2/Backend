# 前后端接口对应关系文档

## 项目概述
本文档旨在梳理前端代码与后端API接口的对应关系，找出不匹配的地方，以便进行调整和整合。

## 基础URL
- 前端设置的baseURL: `/api`
- 后端API实际基础URL: `http://localhost:8080`
- **注意**: 前端的baseURL设置可能导致请求URL重复添加`/api`前缀，需要调整。

## 接口对应关系

### 1. 文章相关接口

| 功能 | 前端API函数 | 前端调用示例 | 后端接口 | 匹配状态 | 备注 |
|------|------------|------------|---------|---------|------|
| 获取文章详情 | `getArticleById(id)` | `getArticleById(1)` | `/api/articles/{id}` | ✅ | 基本匹配，但后端返回结构可能与前端期望不一致 |
| 搜索文章 | `searchArticles(keyword)` | `searchArticles('java')` | `/api/articles/search?keyword={keyword}&page={page}&size={size}` | ❌ | 前端缺少分页参数 |
| 获取文章列表 | 未在api.js中定义 | - | `/api/articles?page={page}&size={size}` | ❌ | 前端未实现 |
| 获取分类文章 | 未在api.js中定义 | - | `/api/articles/category/{category}?page={page}&size={size}` | ❌ | 前端未实现 |
| 创建文章 | 未在api.js中定义 | - | `/api/articles/admin` | ❌ | 前端未实现 |
| 更新文章 | 未在api.js中定义 | - | `/api/articles/admin/{id}` | ❌ | 前端未实现 |

### 2. 标签相关接口

| 功能 | 前端API函数 | 前端调用示例 | 后端接口 | 匹配状态 | 备注 |
|------|------------|------------|---------|---------|------|
| 获取所有标签 | `getTags()` | `getTags()` | `/api/tags` | ✅ | 基本匹配 |
| 获取文章标签 | 未在api.js中定义 | - | `/api/tags/articles/{articleId}` | ❌ | 前端未实现 |
| 为文章添加标签 | 未在api.js中定义 | - | `/api/tags/admin/articles/{articleId}/tags` | ❌ | 前端未实现 |
| 删除文章标签 | 未在api.js中定义 | - | `/api/tags/admin/articles/{articleId}/tags/{tagId}` | ❌ | 前端未实现 |
| 根据标签获取文章 | 未在api.js中定义 | - | `/api/tags/{tagName}/articles?page={page}&size={size}` | ❌ | 前端未实现 |

### 3. 分类相关接口

| 功能 | 前端API函数 | 前端调用示例 | 后端接口 | 匹配状态 | 备注 |
|------|------------|------------|---------|---------|------|
| 获取所有分类 | `getCategories()` | `getCategories()` | 无对应接口 | ❌ | 后端没有获取所有分类的接口，只有获取分类文章的接口 |
| 获取分类文章 | 未在api.js中定义 | - | `/api/articles/category/{category}?page={page}&size={size}` | ❌ | 前端未实现 |

### 4. 附件相关接口

| 功能 | 前端API函数 | 前端调用示例 | 后端接口 | 匹配状态 | 备注 |
|------|------------|------------|---------|---------|------|
| 获取文章附件 | `getAttachments(articleId)` | `getAttachments(1)` | `/api/attachments/admin/articles/{id}/attachments` | ❌ | 后端接口需要管理员权限，且URL不匹配 |
| 下载附件 | `downloadAttachment(attachmentId)` | `downloadAttachment(1)` | 无明确对应接口 | ❌ | 后端未提供明确的下载接口 |
| 上传附件 | 未在api.js中定义 | - | `/api/attachments/admin/articles/attachments` | ❌ | 前端未实现 |
| 更新附件 | 未在api.js中定义 | - | `/api/attachments/admin/articles/{id}/attachments` | ❌ | 前端未实现 |

### 5. 认证相关接口

| 功能 | 前端API函数 | 前端调用示例 | 后端接口 | 匹配状态 | 备注 |
|------|------------|------------|---------|---------|------|
| 管理员登录 | 未在api.js中定义 | - | `/api/admin/login` | ❌ | 前端未实现 |
| 游客登录 | 未在api.js中定义 | - | `/api/guest/login` | ❌ | 前端未实现 |

## 主要问题与调整建议

1. **API基础路径问题**
   - 前端`api.js`中设置的baseURL为`/api`，但后端API路径已经包含`/api`前缀
   - 建议将前端baseURL改为空字符串或`http://localhost:8080`

2. **接口缺失问题**
   - 前端缺少多个关键接口的实现，如获取文章列表、创建文章、更新文章等
   - 建议根据后端接口文档补充缺失的API函数

3. **参数传递问题**
   - 前端搜索文章时未传递分页参数
   - 建议修改`searchArticles`函数，添加分页参数

4. **权限问题**
   - 部分后端接口需要管理员权限，但前端未实现认证逻辑
   - 建议实现JWT认证机制，在请求头中添加Authorization字段

5. **数据结构不匹配**
   - 后端返回的数据结构可能与前端期望不一致（如文章列表、标签等）
   - 建议前端根据后端实际返回的数据结构进行调整