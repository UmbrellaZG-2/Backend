package com.website.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import com.website.backend.constant.HttpStatusConstants;
import org.springframework.web.bind.annotation.*;

import com.website.backend.entity.Article;
import com.website.backend.entity.ArticleTag;
import com.website.backend.entity.Comment;
import com.website.backend.entity.Tag;
import com.website.backend.model.ApiResponse;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.repository.CommentRepository;
import com.website.backend.repository.TagRepository;
import com.website.backend.repository.ArticleTagRepository;
import com.website.backend.DTO.ArticleDTO;
import com.website.backend.DTO.ArticleListDTO;
import com.website.backend.DTO.DeleteArticleResponseDTO;
import com.website.backend.util.DTOConverter;
import jakarta.servlet.http.HttpServletRequest;

import com.website.backend.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

	private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

	private final ArticleRepository articleRepo;

	private final DTOConverter dtoConverter;

	private final CommentRepository commentRepository;

	private final TagRepository tagRepository;

	private final ArticleTagRepository articleTagRepository;

	public ArticleController(ArticleRepository articleRepo, DTOConverter dtoConverter,
			CommentRepository commentRepository, TagRepository tagRepository,
			ArticleTagRepository articleTagRepository) {
		this.articleRepo = articleRepo;
		this.dtoConverter = dtoConverter;
		this.commentRepository = commentRepository;
		this.tagRepository = tagRepository;
		this.articleTagRepository = articleTagRepository;
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

	@GetMapping({ "", "/" })
	// 文章列表页 - 分页加载，返回的文章对象包含addPicture字段，前端可根据该字段决定是否显示封面图片
	public ApiResponse<ArticleListDTO> articles(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		logger.info("获取文章列表，页码: {}, 每页数量: {}", page, size);
		// 使用分页查询
		Pageable pageable = PageRequest.of(page, size);
		Page<Article> articlePage = articleRepo.findAll(pageable);
		ArticleListDTO articleListDTO = buildArticleListDTO(articlePage);
		logger.info("成功获取文章列表，共 {} 页，当前第 {} 页", articleListDTO.getTotalPages(), articleListDTO.getCurrentPage());
		return ApiResponse.success(articleListDTO);
	}

	// 根据文章标题搜索文章
	@GetMapping("/search")
	public ApiResponse<ArticleListDTO> searchArticles(@RequestParam String keyword,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("搜索文章，关键词: {}, 页码: {}, 每页数量: {}", keyword, page, size);
		// 使用分页查询
		Pageable pageable = PageRequest.of(page, size);
		Page<Article> articlePage = articleRepo.findByTitleContaining(keyword, pageable);
		ArticleListDTO articleListDTO = buildArticleListDTO(articlePage);
		logger.info("成功搜索文章，共 {} 页，当前第 {} 页", articleListDTO.getTotalPages(), articleListDTO.getCurrentPage());
		return ApiResponse.success(articleListDTO);
	}

	// 文章详情页
	@GetMapping("/{id}")
	public ApiResponse<ArticleDTO> articleDetails(@PathVariable Long id) {
		logger.info("获取文章详情，ID: {}", id);
		Article article = articleRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

		// 将Article转换为ArticleDTO
		ArticleDTO dto = dtoConverter.convertToDTO(article);
		logger.info("成功获取文章详情，标题: {}", dto.getTitle());
		return ApiResponse.success(dto);
	}

	// 文章分类页
	@GetMapping("/category/{category}")
	public ApiResponse<ArticleListDTO> articlesByCategory(@PathVariable String category,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("获取分类 [{}] 文章列表，页码: {}, 每页数量: {}", category, page, size);
		// 使用分页查询按分类获取文章
		Pageable pageable = PageRequest.of(page, size);
		Page<Article> articlePage = articleRepo.findByCategory(category, pageable);
		ArticleListDTO articleListDTO = buildArticleListDTO(articlePage);
		logger.info("成功获取分类 [{}] 文章列表，共 {} 页，当前第 {} 页", category, articleListDTO.getTotalPages(),
				articleListDTO.getCurrentPage());
		return ApiResponse.success(articleListDTO);
	}

	// 添加评论
	@PostMapping("/{articleId}/comments")
	public ApiResponse<Comment> addComment(@PathVariable Long articleId, @RequestParam String content,
			@RequestParam(required = false) Long parentId, HttpServletRequest request) {
		logger.info("添加评论，文章ID: {}", articleId);
		try {
			// 检查文章是否存在
			Article article = articleRepo.findById(articleId).orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

			Comment comment = new Comment();
			comment.setArticleId(articleId);
			comment.setParentId(parentId);
			// 生成固定前缀+UUID的昵称
			String nickname = "Bro有话说" + UUID.randomUUID().toString().substring(0, 8);
			comment.setNickname(nickname);
			comment.setContent(content);
			comment.setCreateTime(LocalDateTime.now());
			comment.setIpAddress(request.getRemoteAddr());

			Comment savedComment = commentRepository.save(comment);
			logger.info("评论添加成功，ID: {}", savedComment.getId());
			return ApiResponse.success(savedComment);
		}
		catch (ResourceNotFoundException e) {
			logger.error("添加评论失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
		}
		catch (Exception e) {
			logger.error("添加评论失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "添加评论失败: " + e.getMessage());
		}
	}

	// 获取文章的所有评论
	@GetMapping("/{articleId}/comments")
	public ApiResponse<List<Comment>> getArticleComments(@PathVariable Long articleId) {
		logger.info("获取文章评论，文章ID: {}", articleId);
		try {
			// 检查文章是否存在
			Article article = articleRepo.findById(articleId).orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

			List<Comment> comments = commentRepository.findByArticleId(articleId);
			logger.info("成功获取文章评论，数量: {}", comments.size());
			return ApiResponse.success(comments);
		}
		catch (ResourceNotFoundException e) {
			logger.error("获取文章评论失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
		}
		catch (Exception e) {
			logger.error("获取文章评论失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "获取文章评论失败: " + e.getMessage());
		}
	}

	// 获取文章的所有标签
	@GetMapping("/{articleId}/tags")
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
	@GetMapping("/tags")
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
	@GetMapping("/tag/{tagName}")
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
