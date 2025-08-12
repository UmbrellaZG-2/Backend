import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Calendar, Tag, Download, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { downloadAttachment } from '@/services/api-adjusted';

/**
 * 文章详情组件（美化+移动端适配）
 */
const ArticleDetail = ({ article }) => {
  const navigate = useNavigate();

  // 下载附件
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
    return <div className="text-center text-gray-500 py-12 text-lg">文章未找到</div>;
  }

  const safeTags = Array.isArray(article.tags) ? article.tags : [];
  const safeAttachments = Array.isArray(article.attachments) ? article.attachments : [];

  return (
    <div className="max-w-4xl mx-auto px-2 md:px-0 py-4 sm:py-8">
      <Button 
        variant="outline" 
        className="mb-6 sm:mb-8 flex items-center hover:bg-blue-50 text-sm sm:text-base px-3 sm:px-6 py-1 sm:py-2"
        onClick={() => navigate('/')}
      >
        <ArrowLeft className="w-4 h-4 mr-2" />
        返回首页
      </Button>
      <article className="prose prose-base sm:prose-lg max-w-none bg-white rounded-2xl shadow p-4 sm:p-8 md:p-12">
        <h1 className="text-2xl sm:text-4xl font-extrabold mb-4 sm:mb-6 text-gray-900 tracking-tight leading-tight">
          {article.title}
        </h1>
        <div className="flex flex-wrap items-center gap-4 sm:gap-6 mb-6 sm:mb-8 text-gray-600 text-xs sm:text-base">
          <div className="flex items-center">
            <Calendar className="w-4 sm:w-5 h-4 sm:h-5 mr-2 text-blue-400" />
            {article.createTime ? new Date(article.createTime).toLocaleDateString() : ''}
          </div>
          <div className="flex items-center">
            <span className="font-medium text-blue-700">{article.author}</span>
          </div>
        </div>
        {article.picture && (
          <img
            src={article.picture}
            alt={article.title}
            className="w-full h-48 sm:h-96 object-cover rounded-2xl shadow-lg mb-6 sm:mb-10 border"
          />
        )}
        <div className="mb-6 sm:mb-10">
          <div className="flex flex-wrap gap-2 sm:gap-3 mb-3 sm:mb-4">
            {safeTags.map((tag) => (
              <span
                key={tag.id}
                className="flex items-center text-xs sm:text-sm font-semibold bg-gradient-to-r from-blue-200 to-blue-400 text-blue-900 px-3 sm:px-4 py-1 rounded-full shadow hover:from-blue-300 hover:to-blue-500 transition-colors"
              >
                <Tag className="w-3 sm:w-4 h-3 sm:h-4 mr-1" />
                {tag.name}
              </span>
            ))}
          </div>
        </div>
        <div
          className="mb-8 sm:mb-12 text-gray-800 leading-relaxed text-sm sm:text-base"
          dangerouslySetInnerHTML={{ __html: article.content }}
        />
        {safeAttachments.length > 0 && (
          <Card className="mb-6 sm:mb-8 bg-gradient-to-br from-blue-50 to-blue-100 border-0 shadow-md rounded-xl">
            <CardContent className="pt-4 sm:pt-6">
              <h3 className="text-lg sm:text-xl font-bold mb-3 sm:mb-4 text-blue-800">附件下载</h3>
              <div className="space-y-2 sm:space-y-3">
                {safeAttachments.map((attachment) => (
                  <div key={attachment.id} className="flex flex-col sm:flex-row items-start sm:items-center justify-between p-2 sm:p-3 bg-white rounded-lg shadow-sm border hover:bg-blue-50 transition-colors">
                    <div className="mb-2 sm:mb-0">
                      <h4 className="font-medium text-gray-900 text-sm sm:text-base">{attachment.name}</h4>
                      <p className="text-xs text-gray-500">{attachment.size} • 下载次数: {attachment.downloadCount}</p>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      className="hover:bg-blue-100 mt-1 sm:mt-0"
                      onClick={() => handleDownload(attachment.id, attachment.name)}
                    >
                      <Download className="w-4 h-4 mr-2 text-blue-500" />
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
