package com.website.backend.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.website.backend.DTO.ArticleDTO;
import com.website.backend.entity.Article;

/**
 * DTO转换工具类 用于在实体类和DTO之间进行转换
 */
@Component
public class DTOConverter {

	private final ModelMapper modelMapper;

	public DTOConverter() {
		this.modelMapper = new ModelMapper();
	}

	/**
	 * 将Article实体转换为ArticleDTO
	 * @param article 文章实体
	 * @return 文章DTO
	 */
	public ArticleDTO convertToDTO(Article article) {
		return modelMapper.map(article, ArticleDTO.class);
	}

	/**
	 * 将ArticleDTO转换为Article实体
	 * @param dto 文章DTO
	 * @return 文章实体
	 */
	public Article convertToEntity(ArticleDTO dto) {
		return modelMapper.map(dto, Article.class);
	}

}