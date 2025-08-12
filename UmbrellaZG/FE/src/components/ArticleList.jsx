import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tag } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

/**
 * 文章列表组件（美化+移动端适配）
 */
const ArticleList = ({ articles = [] }) => {
  const navigate = useNavigate();
  const safeArticles = Array.isArray(articles) ? articles : [];

  const handleArticleClick = (id) => {
    navigate(`/article/${id}`);
  };

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6 lg:gap-8 p-2 sm:p-4">
      {safeArticles.map((article) => (
        <Card
          key={article.id}
          className="group relative overflow-hidden rounded-2xl shadow-md border-0 bg-gradient-to-br from-white via-blue-50 to-blue-100 hover:shadow-2xl transition-all duration-300 cursor-pointer"
          onClick={() => handleArticleClick(article.id)}
        >
          {/* 封面图片，移动端高度自适应 */}
          {article.picture && (
            <div className="overflow-hidden rounded-t-2xl">
              <img
                src={article.picture}
                alt={article.title}
                className="w-full h-40 sm:h-48 object-cover group-hover:scale-105 transition-transform duration-300"
              />
            </div>
          )}
          <CardHeader className="pb-2">
            <CardTitle className="text-lg sm:text-xl lg:text-2xl font-bold line-clamp-2 text-gray-900 group-hover:text-blue-700 transition-colors">
              {article.title}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3 sm:mb-4 line-clamp-2 sm:line-clamp-3 min-h-[2.5em] sm:min-h-[3.5em] text-sm sm:text-base">{article.summary}</p>
            <div className="flex flex-wrap gap-1 sm:gap-2 mb-3 sm:mb-4">
              {Array.isArray(article.tags) && article.tags.map((tag) => (
                <span
                  key={tag.id}
                  className="flex items-center text-xs font-semibold bg-gradient-to-r from-blue-200 to-blue-400 text-blue-900 px-2 sm:px-3 py-1 rounded-full shadow-sm hover:from-blue-300 hover:to-blue-500 transition-colors"
                >
                  <Tag className="w-3 h-3 mr-1" />
                  {tag.name}
                </span>
              ))}
            </div>
            <div className="flex justify-between text-xs text-gray-500">
              <div className="flex items-center">
                <svg className="w-4 h-4 mr-1 text-blue-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                {article.createTime ? new Date(article.createTime).toLocaleDateString() : ''}
              </div>
            </div>
          </CardContent>
          {/* 渐变底部装饰条 */}
          <div className="absolute bottom-0 left-0 w-full h-1 bg-gradient-to-r from-blue-300 via-blue-400 to-blue-500 opacity-70" />
        </Card>
      ))}
    </div>
  );
};

export default ArticleList;
