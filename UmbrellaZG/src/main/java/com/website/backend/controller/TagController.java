package com.website.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.website.backend.constant.HttpStatusConstants;
import com.website.backend.entity.Article;
import com.website.backend.entity.Tag;
import com.website.backend.entity.ArticleTag;
import com.website.backend.model.ApiResponse;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.repository.TagRepository;
import com.website.backend.repository.ArticleTagRepository;
import com.website.backend.DTO.ArticleDTO;
import com.website.backend.DTO.ArticleListDTO;
import com.website.backend.util.DTOConverter;
import com.website.backend.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/tags")
public class TagController {

	private static final Logger logger = LoggerFactory.getLogger(TagController.class);

	private final ArticleRepository articleRepo;

	private final TagRepository tagRepository;

	private final ArticleTagRepository articleTagRepository;

	private final DTOConverter dtoConverter;

	public TagController(ArticleRepository articleRepo, TagRepository tagRepository,
			ArticleTagRepository articleTagRepository, DTOConverter dtoConverter) {
		this.articleRepo = articleRepo;
		this.tagRepository = tagRepository;
		this.articleTagRepository = articleTagRepository;
		this.dtoConverter = dtoConverter;
	}

	// 抽取公共方法处理分页和DTO转换
	private ArticleListDTO buildArticleListDTO(Page<Article> articlePage) {
		List<ArticleDTO> articleDTOList = articlePage.getContent()
			.stream()
			.map(dtoConverter::convertToDTO)
			.collect(java.util.stream.Collectors.toList());

		ArticleListDTO articleListDTO = new ArticleListDTO();
		articleListDTO.setArticles(articleDTOList);
		articleListDTO.setTotalArticles((int) articlePage.getTotalElements());
		articleListDTO.setTotalPages(articlePage.getTotalPages());
		articleListDTO.setCurrentPage(articlePage.getNumber());
		articleListDTO.setPageSize(articlePage.getSize());
		return articleListDTO;
	}

	// 删除文章的标签
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/admin/articles/{articleId}/tags/{tagId}")
	public ApiResponse<String> deleteArticleTag(@PathVariable Long articleId, @PathVariable Long tagId) {
		logger.info("删除文章标签，文章ID: {}, 标签ID: {}", articleId, tagId);
		try {
			// 检查文章是否存在
			Article article = articleRepo.findById(articleId).orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

			// 检查标签是否存在
			Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new ResourceNotFoundException("标签不存在"));

			// 检查文章和标签是否关联
			List<ArticleTag> articleTags = articleTagRepository.findByArticleId(articleId);
			boolean isAssociated = articleTags.stream().anyMatch(at -> at.getTagId().equals(tagId));

			if (!isAssociated) {
				throw new ResourceNotFoundException("文章和标签未关联");
			}

			// 删除关联
			articleTagRepository.deleteByArticleIdAndTagId(articleId, tagId);

			// 检查是否还有其他文章使用该标签
			List<ArticleTag> otherArticleTags = articleTagRepository.findByTagId(tagId);
			if (otherArticleTags.isEmpty()) {
				// 如果没有其他文章使用该标签，则删除标签
				tagRepository.delete(tag);
				logger.info("标签已删除，标签ID: {}", tagId);
			}

			logger.info("文章标签删除成功");
			return ApiResponse.success("文章标签删除成功");
		}
		catch (ResourceNotFoundException e) {
			logger.error("删除文章标签失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
		}
		catch (Exception e) {
			logger.error("删除文章标签失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "删除文章标签失败: " + e.getMessage());
		}
	}

	// 为文章添加标签
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin/articles/{articleId}/tags")
	public ApiResponse<List<Tag>> addArticleTags(@PathVariable Long articleId, @RequestBody List<String> tagNames) {
		logger.info("为文章添加标签，文章ID: {}, 标签数量: {}", articleId, tagNames.size());
		try {
			// 检查文章是否存在
			Article article = articleRepo.findById(articleId).orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

			List<Tag> tags = new ArrayList<>();
			for (String tagName : tagNames) {
				// 查找或创建标签
				Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
					Tag newTag = new Tag();
					newTag.setName(tagName);
					newTag.setCreateTime(LocalDateTime.now());
					return tagRepository.save(newTag);
				});
				tags.add(tag);
			}

			logger.info("文章标签添加成功");
			return ApiResponse.success(tags);
		}
		catch (ResourceNotFoundException e) {
			logger.error("添加文章标签失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
		}
		catch (Exception e) {
			logger.error("添加文章标签失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "添加文章标签失败: " + e.getMessage());
		}
	}

	// 获取文章的所有标签
	@GetMapping("/articles/{articleId}")
	public ApiResponse<List<Tag>> getArticleTags(@PathVariable Long articleId) {
		logger.info("获取文章标签，文章ID: {}", articleId);
		try {
			// 检查文章是否存在
			Article article = articleRepo.findById(articleId).orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

			// 通过article_tags关联表查询标签ID
			List<ArticleTag> articleTags = articleTagRepository.findByArticleId(articleId);
			List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());

			// 根据标签ID查询标签信息
			List<Tag> tags = new ArrayList<>();
			if (!tagIds.isEmpty()) {
				tags = tagRepository.findAllById(tagIds);
			}

			logger.info("成功获取文章标签，数量: {}", tags.size());
			return ApiResponse.success(tags);
		}
		catch (ResourceNotFoundException e) {
			logger.error("获取文章标签失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
		}
		catch (Exception e) {
			logger.error("获取文章标签失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "获取文章标签失败: " + e.getMessage());
		}
	}

	// 获取所有标签
	@GetMapping
	public ApiResponse<List<Tag>> getAllTags() {
		logger.info("获取所有标签");
		try {
			List<Tag> tags = tagRepository.findAll();
			logger.info("成功获取所有标签，数量: {}", tags.size());
			return ApiResponse.success(tags);
		}
		catch (Exception e) {
			logger.error("获取所有标签失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "获取所有标签失败: " + e.getMessage());
		}
	}

	// 根据标签获取文章列表
	@GetMapping("/{tagName}/articles")
	public ApiResponse<ArticleListDTO> articlesByTag(@PathVariable String tagName,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("获取标签 [{}] 文章列表，页码: {}, 每页数量: {}", tagName, page, size);
		try {
			// 查找标签
			Tag tag = tagRepository.findByName(tagName).orElseThrow(() -> new ResourceNotFoundException("标签不存在"));

			// 通过article_tags关联表查询文章ID
			List<ArticleTag> articleTags = articleTagRepository.findByTagId(tag.getId());
			List<Long> articleIds = articleTags.stream().map(ArticleTag::getArticleId).toList();

			// 根据文章ID查询文章信息并分页
			Pageable pageable = PageRequest.of(page, size);
			Page<Article> articlePage;
			if (articleIds.isEmpty()) {
				articlePage = Page.empty(pageable);
			}
			else {
				articlePage = articleRepo.findByIdIn(new ArrayList<>(articleIds), pageable);
			}

			ArticleListDTO articleListDTO = buildArticleListDTO(articlePage);
			logger.info("成功获取标签 [{}] 文章列表，共 {} 页，当前第 {} 页", tagName, articleListDTO.getTotalPages(),
					articleListDTO.getCurrentPage());
			return ApiResponse.success(articleListDTO);
		}
		catch (ResourceNotFoundException e) {
			logger.error("获取标签文章列表失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
		}
		catch (Exception e) {
			logger.error("获取标签文章列表失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "获取标签文章列表失败: " + e.getMessage());
		}
	}

}