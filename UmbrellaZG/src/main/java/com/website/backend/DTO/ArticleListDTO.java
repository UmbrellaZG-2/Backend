package com.website.backend.DTO;

import lombok.Data;
import java.util.List;

/**
 * 文章列表数据传输对象 用于封装分页文章列表数据在前后端之间传输
 */
@Data
public class ArticleListDTO {

	private List<ArticleDTO> articles;

	private int totalArticles;

	private int totalPages;

	private int currentPage;

	private int pageSize;

}