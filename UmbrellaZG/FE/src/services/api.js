import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 假设后端API路径为/api
  timeout: 10000,
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加认证token等
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
    return Promise.reject(error);
  }
);

// API端点
export const getArticleById = (id) => api.get(`/articles/${id}`);
export const getCategories = () => api.get('/categories');
export const getTags = () => api.get('/tags');
export const searchArticles = (keyword) => api.get('/search', { params: { keyword } });
export const getAttachments = (articleId) => api.get(`/articles/${articleId}/attachments`);
export const downloadAttachment = (attachmentId) => api.get(`/attachments/${attachmentId}/download`, { responseType: 'blob' });

export default api;
