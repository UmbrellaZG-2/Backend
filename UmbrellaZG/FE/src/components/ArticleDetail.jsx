import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Calendar, Clock, Tag, Download, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { downloadAttachment } from '@/services/api-adjusted';

const ArticleDetail = ({ article }) => {
  const navigate = useNavigate();

  const handleDownload = async (attachmentId, fileName) => {
    try {
      const blob = await downloadAttachment(attachmentId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('下载失败:', error);
      alert('下载失败，请重试');
    }
  };

  if (!article) {
    return <div>文章未找到</div>;
  }

  // 确保tags是数组类型
  const safeTags = Array.isArray(article.tags) ? article.tags : [];
  // 确保attachments是数组类型
  const safeAttachments = Array.isArray(article.attachments) ? article.attachments : [];

  return (
    <div className="max-w-4xl mx-auto">
      <Button 
        variant="outline" 
        className="mb-6 flex items-center"
        onClick={() => navigate('/')}
      >
        <ArrowLeft className="w-4 h-4 mr-2" />
        返回首页
      </Button>
      
      <article className="prose prose-lg max-w-none">
        <h1 className="text-3xl font-bold mb-4">{article.title}</h1>
        
        <div className="flex flex-wrap items-center gap-4 mb-6 text-gray-600">
          <div className="flex items-center">
            <Calendar className="w-4 h-4 mr-1" />
            {new Date(article.createdAt).toLocaleDateString()}
          </div>
          <div className="flex items-center">
            <Clock className="w-4 h-4 mr-1" />
            {article.readTime}分钟阅读
          </div>
          <div className="flex items-center">
            <span className="font-medium">{article.author}</span>
          </div>
        </div>
        
        {article.coverImage && (
          <img 
            src={article.coverImage} 
            alt={article.title} 
            className="w-full h-96 object-cover rounded-lg mb-8"
          />
        )}
        
        <div className="mb-8">
          <div className="flex flex-wrap gap-2 mb-6">
            {safeTags.map((tag) => (
              <span key={tag.id} className="flex items-center text-sm bg-blue-100 text-blue-800 px-3 py-1 rounded-full">
                <Tag className="w-3 h-3 mr-1" />
                {tag.name}
              </span>
            ))}
          </div>
        </div>
        
        <div 
          className="mb-8"
          dangerouslySetInnerHTML={{ __html: article.content }} 
        />
        
        {safeAttachments.length > 0 && (
          <Card className="mb-8">
            <CardContent className="pt-6">
              <h3 className="text-xl font-semibold mb-4">附件</h3>
              <div className="space-y-3">
                {safeAttachments.map((attachment) => (
                  <div key={attachment.id} className="flex items-center justify-between p-3 border rounded-lg">
                    <div>
                      <h4 className="font-medium">{attachment.name}</h4>
                      <p className="text-sm text-gray-600">{attachment.size} • 下载次数: {attachment.downloadCount}</p>
                    </div>
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => handleDownload(attachment.id, attachment.name)}
                    >
                      <Download className="w-4 h-4 mr-2" />
                      下载
                    </Button>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}
      </article>
    </div>
  );
};

export default ArticleDetail;
