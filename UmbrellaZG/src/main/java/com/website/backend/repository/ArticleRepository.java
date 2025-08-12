package com.website.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.website.backend.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

	// 按分类查询文章
	Page<Article> findByCategory(String category, Pageable pageable);

	// 分页查询所有文章
	@Override
	Page<Article> findAll(Pageable pageable);

	// 根据ID列表查询文章
	Page<Article> findByIdIn(List<Long> ids, Pageable pageable);

	// 根据文章标题模糊搜索
	Page<Article> findByTitleContaining(String title, Pageable pageable);

}
