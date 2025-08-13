// 创建axios实例
import axios from 'axios';

const api = axios.create({
  baseURL: '', // 移除/api前缀，避免重复添加
  timeout: 10000,
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加认证token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    console.error('API Error:', error);
    // 处理认证错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// API端点
// 文章相关
export const getArticleById = (id) => api.get(`/api/articles/${id}`);
export const getArticles = (page = 0, size = 10) => api.get(`/api/articles?page=${page}&size=${size}`);
export const searchArticles = (keyword, page = 0, size = 10) => api.get(`/api/articles/search?keyword=${keyword}&page=${page}&size=${size}`);
export const getArticlesByCategory = (category, page = 0, size = 10) => api.get(`/api/articles/category/${category}?page=${page}&size=${size}`);

// 分类相关 - 添加这部分
export const getCategories = () => api.get('/api/categories');

// 标签相关
export const getTags = () => api.get('/api/tags');
export const getArticleTags = (articleId) => api.get(`/api/tags/articles/${articleId}`);
export const getArticlesByTag = (tagName, page = 0, size = 10) => api.get(`/api/tags/${tagName}/articles?page=${page}&size=${size}`);

export const addArticleTags = (articleId, tagNames) => api.post(`/api/tags/admin/articles/${articleId}/tags`, tagNames);
export const deleteArticleTag = (articleId, tagId) => api.delete(`/api/tags/admin/articles/${articleId}/tags/${tagId}`);

// 附件相关
export const getArticleAttachments = (articleId) => api.get(`/api/attachments/admin/articles/${articleId}/attachments`);
export const downloadAttachment = (attachmentId) => api.get(`/api/attachments/${attachmentId}/download`, { responseType: 'blob' });

// 认证相关
export const adminLogin = (credentials) => api.post('/api/auth/admin/login', credentials);
export const guestLogin = () => api.post('/api/auth/guest/login');

export default api;