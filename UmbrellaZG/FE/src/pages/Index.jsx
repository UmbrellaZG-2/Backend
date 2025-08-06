import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getTags, searchArticles, getArticles, getArticlesByCategory, getArticlesByTag, getCategories } from '@/services/api-adjusted';
import { useNavigate } from 'react-router-dom';
import ArticleList from '@/components/ArticleList';
import SearchBar from '@/components/SearchBar';
import CategoryNav from '@/components/CategoryNav';
import TagCloud from '@/components/TagCloud';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';

const Index = () => {
  const navigate = useNavigate();
  const [activeCategory, setActiveCategory] = useState(null);
  const [activeTag, setActiveTag] = useState(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  // 获取文章列表
  const { data: articles, isLoading: articlesLoading, error: articlesError } = useQuery({
    queryKey: ['articles', page, size, activeCategory, activeTag],
    queryFn: () => {
      if (activeCategory) {
        return getArticlesByCategory(activeCategory, page, size);
      } else if (activeTag) {
        return getArticlesByTag(activeTag, page, size);
      }
      return getArticles(page, size);
    },
  });

  // 搜索文章
  const { data: searchResults, isLoading: searchLoading, error: searchError } = useQuery({
    queryKey: ['search', searchKeyword, page, size],
    queryFn: () => searchArticles(searchKeyword, page, size),
    enabled: !!searchKeyword, // 只有当搜索关键词存在时才执行查询
  });

  // 获取分类
  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
  });

  // 确保categories是数组类型
  const safeCategories = Array.isArray(categories) ? categories : [];

  // 获取标签
  const { data: tags, isLoading: tagsLoading } = useQuery({
    queryKey: ['tags'],
    queryFn: getTags,
  });

  const handleSearch = (keyword) => {
    setSearchKeyword(keyword);
    setActiveCategory(null);
    setActiveTag(null);
    setPage(0);
  };

  const handleCategoryChange = (category) => {
    setActiveCategory(category);
    setActiveTag(null);
    setSearchKeyword('');
    setPage(0);
  };



  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  // 处理分页和无结果情况
  const isEmpty = (!searchResults || searchResults.articles?.length === 0) && 
                 (!articles || articles.articles?.length === 0) && 
                 !searchLoading && !articlesLoading;

  const hasResults = searchResults?.articles?.length > 0 || articles?.articles?.length > 0;

  // 提取文章数据
  const displayArticles = searchKeyword ? 
    (searchResults?.articles || []) : 
    (articles?.articles || []);

  // 分页信息
  const totalPages = searchKeyword ? 
    (searchResults?.totalPages || 0) : 
    (articles?.totalPages || 0);

  const totalArticles = searchKeyword ? 
    (searchResults?.totalArticles || 0) : 
    (articles?.totalArticles || 0);

  const currentPage = searchKeyword ? 
    (searchResults?.currentPage || 0) : 
    page;

  const handleTagClick = (tag) => {
    setActiveTag(tag.name);
    setActiveCategory(null);
    setSearchKeyword('');
    setPage(0);
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold mb-4">我的个人博客</h1>
        <p className="text-xl text-gray-600">分享技术与思考</p>
        <div className="flex justify-center gap-4 mt-6">
          <Button onClick={() => navigate('/login')}>管理员登录</Button>
          <Button variant="outline" onClick={() => navigate('/')}>全部文章</Button>
          <Button 
            variant="outline" 
            onClick={() => navigate('/about')}
            className="bg-black text-white hover:bg-gray-800"
          >
            关于我
          </Button>
        </div>
      </div>

      <SearchBar onSearch={handleSearch} />

      {categoriesLoading ? (
        <Skeleton className="h-10 w-full mb-8" />
      ) : (
        <CategoryNav 
          categories={safeCategories} 
          activeCategory={activeCategory} 
          onCategoryChange={handleCategoryChange} 
        />
      )}

      {tagsLoading ? (
        <Skeleton className="h-20 w-full mb-8" />
      ) : (
        <TagCloud 
          tags={Array.isArray(tags) ? tags : []} 
          onTagClick={handleTagClick} 
        />
      )}

      {searchKeyword && (
        <div className="mb-4">
          <h2 className="text-2xl font-bold mb-4">搜索结果: "{searchKeyword}"</h2>
          <p className="text-gray-600 mb-6">找到 {totalArticles} 篇相关文章</p>
        </div>
      )}

      {activeCategory && (
        <div className="mb-4">
          <h2 className="text-2xl font-bold mb-4">分类: {activeCategory}</h2>
          <p className="text-gray-600 mb-6">找到 {totalArticles} 篇相关文章</p>
        </div>
      )}

      {activeTag && (
        <div className="mb-4">
          <h2 className="text-2xl font-bold mb-4">标签: {activeTag}</h2>
          <p className="text-gray-600 mb-6">找到 {totalArticles} 篇相关文章</p>
        </div>
      )}

      {(searchLoading || articlesLoading) && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          {[...Array(6)].map((_, i) => (
            <Skeleton key={i} className="h-80 w-full" />
          ))}
        </div>
      )}

      {(searchError || articlesError) && (
        <div className="text-center text-red-500 mb-8">
          获取文章失败: {(searchError || articlesError).message}
        </div>
      )}

      {isEmpty && !searchLoading && !articlesLoading && (
        <div className="text-center py-12 mb-8">
          <h2 className="text-2xl font-bold mb-4">暂无文章</h2>
          <p className="text-gray-600 mb-6">没有找到符合条件的文章</p>
        </div>
      )}

      {hasResults && (
        <>
          <ArticleList articles={displayArticles} />

          <div className="flex justify-center mt-8">
            <div className="flex items-center space-x-2">
              <Button 
                variant="outline" 
                disabled={currentPage === 0} 
                onClick={() => handlePageChange(currentPage - 1)}
              >
                上一页
              </Button>
              <span className="px-4 py-2">
                {currentPage + 1} / {totalPages}
              </span>
              <Button 
                variant="outline" 
                disabled={currentPage >= totalPages - 1 || totalPages === 0}
                onClick={() => handlePageChange(currentPage + 1)}
              >
                下一页
              </Button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Index;
