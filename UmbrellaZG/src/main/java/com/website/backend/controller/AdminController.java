package com.website.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.website.backend.constant.HttpStatusConstants;
import com.website.backend.entity.Article;
import com.website.backend.entity.ArticleTag;
import com.website.backend.entity.Tag;
import com.website.backend.entity.Attachment;
import com.website.backend.entity.ArticlePicture;
import com.website.backend.exception.ResourceNotFoundException;
import com.website.backend.model.ApiResponse;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.repository.ArticleTagRepository;
import com.website.backend.repository.CommentRepository;
import com.website.backend.repository.TagRepository;
import com.website.backend.repository.AttachmentRepository;
import com.website.backend.repository.ArticlePictureRepository;
import com.website.backend.security.JwtTokenProvider;
import com.website.backend.service.AttachmentService;
import com.website.backend.service.ArticlePictureService;
import com.website.backend.util.DTOConverter;
import com.website.backend.DTO.ArticleDTO;
import com.website.backend.DTO.DeleteArticleResponseDTO;

import java.time.LocalDateTime;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理员控制器 处理管理员相关的API请求
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	private final AuthenticationManager authenticationManager;

	private final JwtTokenProvider jwtTokenProvider;

	private final ArticleRepository articleRepo;

	private final DTOConverter dtoConverter;

	private final CommentRepository commentRepository;

	private final TagRepository tagRepository;

	private final ArticleTagRepository articleTagRepository;

	private final AttachmentRepository attachmentRepository;

	private final ArticlePictureRepository articlePictureRepository;

	private final AttachmentService attachmentService;

	private final ArticlePictureService articlePictureService;

	/**
	 * 构造函数注入依赖
	 */
	public AdminController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
			ArticleRepository articleRepo, DTOConverter dtoConverter, CommentRepository commentRepository,
			TagRepository tagRepository, ArticleTagRepository articleTagRepository,
			AttachmentRepository attachmentRepository, ArticlePictureRepository articlePictureRepository,
			AttachmentService attachmentService, ArticlePictureService articlePictureService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.articleRepo = articleRepo;
		this.dtoConverter = dtoConverter;
		this.commentRepository = commentRepository;
		this.tagRepository = tagRepository;
		this.articleTagRepository = articleTagRepository;
		this.attachmentRepository = attachmentRepository;
		this.articlePictureRepository = articlePictureRepository;
		this.attachmentService = attachmentService;
		this.articlePictureService = articlePictureService;
	}

	/**
	 * 管理员登录
	 */
	@PostMapping("/login")
	public ApiResponse<Map<String, Object>> adminLogin(@RequestBody LoginRequest loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();
		log.info("管理员登录请求: 用户名={}", username);
		try {
			// 进行身份验证
			Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			// 设置身份验证上下文
			SecurityContextHolder.getContext().setAuthentication(authentication);

			// 检查用户是否具有管理员角色
			boolean isAdmin = authentication.getAuthorities()
				.stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

			if (!isAdmin) {
				log.warn("用户 {} 没有管理员权限", username);
				throw new ResourceNotFoundException("用户 " + username + " 没有管理员权限");
			}

			// 生成JWT令牌
			String jwt = jwtTokenProvider.generateToken(authentication);
			log.info("管理员 {} 登录成功", username);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "登录成功");
			response.put("token", jwt);
			return ApiResponse.success(response);
		}
		catch (AuthenticationException e) {
			log.error("管理员登录失败: 用户名={}, 错误={}", username, e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.UNAUTHORIZED, "认证失败: 用户名或密码错误");
		}
		catch (ResourceNotFoundException e) {
			log.error("管理员登录失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.FORBIDDEN, e.getMessage());
		}
		catch (Exception e) {
			log.error("管理员登录发生未知错误: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "登录失败: 服务器内部错误");
		}
	}

	/**
	 * 管理员创建文章（支持附件上传和封面图片上传）
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/articles")
	public ApiResponse<ArticleDTO> createArticle(@RequestParam String title, @RequestParam String category,
			@RequestParam String content) {
		logger.info("开始创建文章，标题: {}", title);
		try {
			// 添加输入验证
			if (title == null || title.trim().isEmpty()) {
				logger.warn("文章标题不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章标题不能为空");
			}
			if (category == null || category.trim().isEmpty()) {
				logger.warn("文章分类不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章分类不能为空");
			}
			if (content == null || content.trim().isEmpty()) {
				logger.warn("文章内容不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章内容不能为空");
			}

			// 创建新文章
			Article article = new Article();
			article.setTitle(title);
			article.setCategory(category);
			article.setContent(content);
			article.setCreateTime(LocalDateTime.now());
			article.setUpdateTime(LocalDateTime.now());
			article.setAddAttach(false);
			article.setAddPicture(false);
			logger.info("文章创建完成，标题: {}", title);

			// 保存文章
			Article savedArticle = articleRepo.save(article);
			logger.info("文章保存成功，ID: {}", savedArticle.getArticleId());

			// 将Article转换为ArticleDTO
			ArticleDTO dto = dtoConverter.convertToDTO(savedArticle);
			logger.info("文章创建完成，返回DTO: {}", dto.getTitle());
			return ApiResponse.success(dto);
		}
		catch (Exception e) {
			logger.error("文章创建失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章创建失败: " + e.getMessage());
		}
	}

	/**
	 * 管理员更新文章（支持附件更新和删除，封面图片更新和删除）
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/articles/{id}")
	public ApiResponse<ArticleDTO> updateArticle(@PathVariable Long id, @RequestParam String title,
			@RequestParam String category, @RequestParam String content) {
		logger.info("开始更新文章，ID: {}", id);
		try {
			Optional<Article> articleOptional = articleRepo.findById(id);
			if (articleOptional.isEmpty()) {
				logger.error("文章不存在: {}", id);
				return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, "文章不存在");
			}

			Article article = articleOptional.get();
			article.setTitle(title);
			article.setCategory(category);
			article.setContent(content);
			logger.info("文章信息更新完成，标题: {}", title);

			// 添加输入验证
			if (title == null || title.trim().isEmpty()) {
				logger.warn("文章标题不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章标题不能为空");
			}
			if (category == null || category.trim().isEmpty()) {
				logger.warn("文章分类不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章分类不能为空");
			}
			if (content == null || content.trim().isEmpty()) {
				logger.warn("文章内容不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章内容不能为空");
			}

			// 保存更新后的文章
			Article updatedArticle = articleRepo.save(article);
			logger.info("文章保存成功，ID: {}", updatedArticle.getArticleId());

			// 将Article转换为ArticleDTO
			ArticleDTO dto = dtoConverter.convertToDTO(updatedArticle);
			logger.info("文章更新完成，返回DTO: {}", dto.getTitle());
			return ApiResponse.success(dto);
		}
		catch (Exception e) {
			logger.error("文章更新失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章更新失败: " + e.getMessage());
		}
	}

	/**
	 * 管理员删除文章（同时删除相关附件）
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/articles/{id}")
	public ApiResponse<DeleteArticleResponseDTO> deleteArticle(@PathVariable Long id) {
		try {
			Optional<Article> articleOptional = articleRepo.findById(id);
			if (articleOptional.isEmpty()) {
				return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, "文章不存在");
			}

			Article article = articleOptional.get();
			// 注意: 附件和图片清理功能已迁移到AttachmentController
			// 删除文章前请先调用 /api/attachments/admin/articles/{id}/cleanup 接口清理附件和图片

			// 删除文章
			articleRepo.deleteById(id);

			DeleteArticleResponseDTO response = new DeleteArticleResponseDTO();
			response.setSuccess(true);
			response.setMessage("文章删除成功");
			return ApiResponse.success(response);
		}
		catch (Exception e) {
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章删除失败: " + e.getMessage());
		}
	}

	/**
	 * 删除文章的标签
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/articles/{articleId}/tags/{tagId}")
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

			// 直接删除标签，不管是否有其他文章使用
			tagRepository.delete(tag);
			logger.info("标签已删除，标签ID: {}", tagId);

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

	/**
	 * 为文章添加标签
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/articles/{articleId}/tags")
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

	/**
	 * 管理员创建文章时上传附件和图片
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/articles/attachments")
	public ApiResponse<ArticleDTO> createArticleWithAttachments(@RequestParam String title,
			@RequestParam String category, @RequestParam String content,
			@RequestParam(required = false) MultipartFile attachment,
			@RequestParam(required = false) MultipartFile picture) {
		logger.info("开始创建文章，标题: {}", title);
		try {
			// 添加输入验证
			if (title == null || title.trim().isEmpty()) {
				logger.warn("文章标题不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章标题不能为空");
			}
			if (category == null || category.trim().isEmpty()) {
				logger.warn("文章分类不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章分类不能为空");
			}
			if (content == null || content.trim().isEmpty()) {
				logger.warn("文章内容不能为空");
				return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文章内容不能为空");
			}

			// 创建新文章
			Article article = new Article();
			article.setTitle(title);
			article.setCategory(category);
			article.setContent(content);
			article.setCreateTime(LocalDateTime.now());
			article.setUpdateTime(LocalDateTime.now());
			article.setAddAttach(attachment != null && !attachment.isEmpty());
			article.setAddPicture(picture != null && !picture.isEmpty());
			logger.info("文章创建完成，标题: {}", title);

			// 保存文章
			Article savedArticle = articleRepo.save(article);
			logger.info("文章保存成功，ID: {}", savedArticle.getArticleId());

			// 上传附件（如果有）
			if (attachment != null && !attachment.isEmpty()) {
				logger.info("开始上传文章附件，文章ID: {}", savedArticle.getArticleId());
				try {
					Attachment newAttachment = attachmentService.uploadAttachment(attachment, savedArticle);
					attachmentRepository.save(newAttachment);
					logger.info("文章附件上传成功");
				}
				catch (IOException e) {
					if (e.getMessage().contains("附件数量超过限制")) {
						logger.warn("文章附件数量超过限制: {}", e.getMessage());
						return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, e.getMessage());
					}
					else {
						throw e;
					}
				}
			}

			// 上传封面图片（如果有）
			if (picture != null && !picture.isEmpty()) {
				logger.info("开始上传文章封面图片，文章ID: {}", savedArticle.getArticleId());
				try {
					ArticlePicture newPicture = articlePictureService.uploadPicture(picture, savedArticle);
					articlePictureRepository.save(newPicture);
					logger.info("文章封面图片上传成功");
				}
				catch (IOException e) {
					// 处理图片上传异常
					logger.error("文章封面图片上传失败: {}", e.getMessage());
					return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR,
							savedArticle.getTitle() + " 文章创建成功，但图片上传失败: " + e.getMessage());
				}
			}

			// 将Article转换为ArticleDTO
			ArticleDTO dto = dtoConverter.convertToDTO(savedArticle);
			logger.info("文章创建完成，返回DTO: {}", dto.getTitle());
			return ApiResponse.success(dto);
		}
		catch (Exception e) {
			logger.error("文章创建失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章创建失败: " + e.getMessage());
		}
	}

	/**
	 * 更新文章时处理附件和图片
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/articles/{id}/attachments")
	public ApiResponse<ArticleDTO> updateArticleAttachments(@PathVariable Long id,
			@RequestParam(required = false) MultipartFile attachment,
			@RequestParam(required = false, defaultValue = "false") boolean deleteAttachment,
			@RequestParam(required = false) MultipartFile picture,
			@RequestParam(required = false, defaultValue = "false") boolean deletePicture) {
		logger.info("开始更新文章附件和图片，ID: {}", id);
		try {
			Optional<Article> articleOptional = articleRepo.findById(id);
			if (articleOptional.isEmpty()) {
				logger.error("文章不存在: {}", id);
				return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, "文章不存在");
			}

			Article article = articleOptional.get();

			// 处理附件
			if (deleteAttachment) {
				logger.info("删除文章附件，文章ID: {}", article.getArticleId());
				attachmentService.deleteAttachmentsByArticle(article);
				article.setAddAttach(false);
				logger.info("文章附件删除成功");
			}
			else if (attachment != null && !attachment.isEmpty()) {
				// 如果已有附件，先删除
				if (article.isAddAttach()) {
					logger.info("删除旧文章附件，文章ID: {}", article.getArticleId());
					attachmentService.deleteAttachmentsByArticle(article);
				}
				article.setAddAttach(true);
				logger.info("准备上传新文章附件");
			} // 否则保持原有状态

			// 处理封面图片
			if (deletePicture) {
				logger.info("删除文章封面图片，文章ID: {}", article.getArticleId());
				articlePictureService.deletePictureByArticle(article);
				article.setAddPicture(false);
				logger.info("文章封面图片删除成功");
			}
			else if (picture != null && !picture.isEmpty()) {
				// 如果已有图片，先删除
				if (article.isAddPicture()) {
					logger.info("删除旧文章封面图片，文章ID: {}", article.getArticleId());
					articlePictureService.deletePictureByArticle(article);
				}
				article.setAddPicture(true);
				logger.info("准备上传新文章封面图片");
			} // 否则保持原有状态

			// 保存更新后的文章
			Article updatedArticle = articleRepo.save(article);
			logger.info("文章保存成功，ID: {}", updatedArticle.getArticleId());

			// 上传附件（如果有）
			if (attachment != null && !attachment.isEmpty()) {
				logger.info("开始上传文章附件，文章ID: {}", updatedArticle.getArticleId());
				try {
					Attachment newAttachment = attachmentService.uploadAttachment(attachment, updatedArticle);
					attachmentRepository.save(newAttachment);
					logger.info("文章附件上传成功");
				}
				catch (IOException e) {
					if (e.getMessage().contains("附件数量超过限制")) {
						logger.warn("文章附件数量超过限制: {}", e.getMessage());
						return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, e.getMessage());
					}
					else {
						throw e;
					}
				}
			}

			// 上传封面图片（如果有）
			if (picture != null && !picture.isEmpty()) {
				logger.info("开始上传文章封面图片，文章ID: {}", updatedArticle.getArticleId());
				try {
					ArticlePicture newPicture = articlePictureService.uploadPicture(picture, updatedArticle);
					articlePictureRepository.save(newPicture);
					logger.info("文章封面图片上传成功");
				}
				catch (IOException e) {
					// 处理图片上传异常
					logger.error("文章封面图片上传失败: {}", e.getMessage());
					return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR,
							updatedArticle.getTitle() + " 文章更新成功，但图片上传失败: " + e.getMessage());
				}
			}

			// 将Article转换为ArticleDTO
			ArticleDTO dto = dtoConverter.convertToDTO(updatedArticle);
			logger.info("文章更新完成，返回DTO: {}", dto.getTitle());
			return ApiResponse.success(dto);
		}
		catch (Exception e) {
			logger.error("文章更新失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章更新失败: " + e.getMessage());
		}
	}

	/**
	 * 删除文章时删除附件和图片
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/articles/{id}/cleanup")
	public ApiResponse<String> cleanupArticleAttachments(@PathVariable Long id) {
		try {
			Optional<Article> articleOptional = articleRepo.findById(id);
			if (articleOptional.isEmpty()) {
				return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, "文章不存在");
			}

			Article article = articleOptional.get();
			// 如果文章有附件，先删除附件
			if (article.isAddAttach()) {
				attachmentService.deleteAttachmentsByArticle(article);
			}

			// 如果文章有封面图片，先删除图片
			if (article.isAddPicture()) {
				articlePictureService.deletePictureByArticle(article);
			}

			logger.info("文章附件和图片清理成功，文章ID: {}", id);
			return ApiResponse.success("文章附件和图片清理成功");
		}
		catch (Exception e) {
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "清理文章附件和图片失败: " + e.getMessage());
		}
	}

	/**
	 * 登录请求参数
	 */
	@Data
	public static class LoginRequest {

		private String username;

		private String password;

	}

}