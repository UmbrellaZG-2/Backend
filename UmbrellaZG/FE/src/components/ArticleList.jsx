import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Calendar, Clock, Tag } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const ArticleList = ({ articles = [] }) => {
  const navigate = useNavigate();
  
  // 确保articles是数组类型
  const safeArticles = Array.isArray(articles) ? articles : [];

  const handleArticleClick = (id) => {
    navigate(`/article/${id}`);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {safeArticles.map((article) => (
        <Card 
          key={article.id} 
          className="hover:shadow-lg transition-shadow cursor-pointer"
          onClick={() => handleArticleClick(article.id)}
        >
          {article.coverImage && (
            <img 
              src={article.coverImage} 
              alt={article.title} 
              className="w-full h-48 object-cover rounded-t-lg"
            />
          )}
          <CardHeader>
            <CardTitle className="text-xl line-clamp-2">{article.title}</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-4 line-clamp-3">{article.summary}</p>
            <div className="flex flex-wrap gap-2 mb-4">
              {Array.isArray(article.tags) && article.tags.map((tag) => (
                <span key={tag.id} className="flex items-center text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                  <Tag className="w-3 h-3 mr-1" />
                  {tag.name}
                </span>
              ))}
            </div>
            <div className="flex justify-between text-sm text-gray-500">
              <div className="flex items-center">
                <Calendar className="w-4 h-4 mr-1" />
                {new Date(article.createdAt).toLocaleDateString()}
              </div>
              <div className="flex items-center">
                <Clock className="w-4 h-4 mr-1" />
                {article.readTime}分钟阅读
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default ArticleList;
