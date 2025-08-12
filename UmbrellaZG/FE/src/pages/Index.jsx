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
    enabled: !!searchKeyword,
  });

  // 获取分类
  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
  });
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

  const isEmpty = (!searchResults || searchResults.articles?.length === 0) &&
                 (!articles || articles.articles?.length === 0) &&
                 !searchLoading && !articlesLoading;
  const hasResults = searchResults?.articles?.length > 0 || articles?.articles?.length > 0;
  const displayArticles = searchKeyword ?
    (searchResults?.articles || []) :
    (articles?.articles || []);
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
    <div className="container mx-auto px-2 sm:px-4 py-4 sm:py-8">
      <div className="text-center mb-8 sm:mb-12">
        <h1 className="text-2xl sm:text-4xl font-bold mb-2 sm:mb-4">我的个人博客</h1>
        <p className="text-base sm:text-xl text-gray-600">分享技术与思考</p>
        <div className="flex flex-wrap justify-center gap-2 sm:gap-4 mt-4 sm:mt-6">
          <Button onClick={() => navigate('/login')} className="text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2">管理员登录</Button>
          <Button variant="outline" onClick={() => navigate('/')} className="text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2">全部文章</Button>
          <Button
            variant="outline"
            onClick={() => navigate('/about')}
            className="bg-black text-white hover:bg-gray-800 text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2"
          >
            关于我
          </Button>
        </div>
      </div>

      <SearchBar onSearch={handleSearch} />

      {categoriesLoading ? (
        <Skeleton className="h-10 w-full mb-6 sm:mb-8" />
      ) : (
        <CategoryNav
          categories={safeCategories}
          activeCategory={activeCategory}
          onCategoryChange={handleCategoryChange}
        />
      )}

      {tagsLoading ? (
        <Skeleton className="h-20 w-full mb-6 sm:mb-8" />
      ) : (
        <TagCloud
          tags={Array.isArray(tags) ? tags : []}
          onTagClick={handleTagClick}
        />
      )}

      {searchKeyword && (
        <div className="mb-3 sm:mb-4">
          <h2 className="text-lg sm:text-2xl font-bold mb-2 sm:mb-4">搜索结果: "{searchKeyword}"</h2>
          <p className="text-gray-600 mb-3 sm:mb-6">找到 {totalArticles} 篇相关文章</p>
        </div>
      )}

      {activeCategory && (
        <div className="mb-3 sm:mb-4">
          <h2 className="text-lg sm:text-2xl font-bold mb-2 sm:mb-4">分类: {activeCategory}</h2>
          <p className="text-gray-600 mb-3 sm:mb-6">找到 {totalArticles} 篇相关文章</p>
        </div>
      )}

      {activeTag && (
        <div className="mb-3 sm:mb-4">
          <h2 className="text-lg sm:text-2xl font-bold mb-2 sm:mb-4">标签: {activeTag}</h2>
          <p className="text-gray-600 mb-3 sm:mb-6">找到 {totalArticles} 篇相关文章</p>
        </div>
      )}

      {(searchLoading || articlesLoading) && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6 mb-6 sm:mb-8">
          {[...Array(6)].map((_, i) => (
            <Skeleton key={i} className="h-60 sm:h-80 w-full" />
          ))}
        </div>
      )}

      {(searchError || articlesError) && (
        <div className="text-center text-red-500 mb-6 sm:mb-8">
          获取文章失败: {(searchError || articlesError).message}
        </div>
      )}

      {isEmpty && !searchLoading && !articlesLoading && (
        <div className="text-center py-8 sm:py-12 mb-6 sm:mb-8">
          <h2 className="text-lg sm:text-2xl font-bold mb-2 sm:mb-4">暂无文章</h2>
          <p className="text-gray-600 mb-3 sm:mb-6">没有找到符合条件的文章</p>
        </div>
      )}

      {hasResults && (
        <>
          <ArticleList articles={displayArticles} />

          <div className="flex justify-center mt-6 sm:mt-8">
            <div className="flex items-center space-x-1 sm:space-x-2">
              <Button
                variant="outline"
                disabled={currentPage === 0}
                onClick={() => handlePageChange(currentPage - 1)}
                className="text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2"
              >
                上一页
              </Button>
              <span className="px-2 sm:px-4 py-1 sm:py-2 text-xs sm:text-base">
                {currentPage + 1} / {totalPages}
              </span>
              <Button
                variant="outline"
                disabled={currentPage >= totalPages - 1 || totalPages === 0}
                onClick={() => handlePageChange(currentPage + 1)}
                className="text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2"
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
