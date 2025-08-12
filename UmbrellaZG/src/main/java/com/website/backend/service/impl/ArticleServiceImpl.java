package com.website.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.website.backend.entity.Article;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.service.ArticleService;
import com.website.backend.service.AttachmentService;
import com.website.backend.service.ArticlePictureService;
import com.website.backend.exception.ResourceNotFoundException;
import com.website.backend.exception.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ArticleService接口的实现类 提供文章的CRUD操作及相关业务逻辑
 */
@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {

	private final ArticleRepository articleRepository;

	private final AttachmentService attachmentService;

	private final ArticlePictureService articlePictureService;

	/**
	 * 构造函数注入依赖
	 * @param articleRepository 文章仓库
	 * @param attachmentService 附件服务
	 * @param articlePictureService 文章图片服务
	 */
	public ArticleServiceImpl(ArticleRepository articleRepository, AttachmentService attachmentService,
			ArticlePictureService articlePictureService) {
		this.articleRepository = articleRepository;
		this.attachmentService = attachmentService;
		this.articlePictureService = articlePictureService;
	}

	/**
	 * 获取所有文章（分页）
	 * @param pageable 分页参数
	 * @return 文章分页列表
	 */
	@Override
	public Page<Article> getAllArticles(Pageable pageable) {
		log.info("获取所有文章，分页参数: {}", pageable);
		return articleRepository.findAll(pageable);
	}

	/**
	 * 根据分类获取文章（分页）
	 * @param category 文章分类
	 * @param pageable 分页参数
	 * @return 分类文章分页列表
	 */
	@Override
	public Page<Article> getArticlesByCategory(String category, Pageable pageable) {
		log.info("获取分类 [{}] 的文章，分页参数: {}", category, pageable);
		return articleRepository.findByCategory(category, pageable);
	}

	/**
	 * 根据ID获取文章
	 * @param id 文章ID
	 * @return 文章详情
	 * @throws ResourceNotFoundException 当文章不存在时抛出
	 */
	@Override
	public Article getArticleById(Long id) {
		log.info("获取文章，ID: {}", id);
		return articleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("文章不存在，ID: " + id));
	}

	/**
	 * 创建新文章
	 * @param title 文章标题
	 * @param category 文章分类
	 * @param content 文章内容
	 * @param attachment 附件
	 * @param picture 封面图片
	 * @return 创建的文章
	 * @throws FileUploadException 当文件上传失败时抛出
	 */
	@Override
	public Article createArticle(String title, String category, String content, MultipartFile attachment,
			MultipartFile picture) {
		log.info("创建新文章，标题: {}", title);

		// 验证输入
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("文章标题不能为空");
		}
		if (category == null || category.trim().isEmpty()) {
			throw new IllegalArgumentException("文章分类不能为空");
		}
		if (content == null || content.trim().isEmpty()) {
			throw new IllegalArgumentException("文章内容不能为空");
		}

		// 创建文章
		Article article = new Article();
		article.setTitle(title);
		article.setCategory(category);
		article.setContent(content);
		article.setArticleId(generateArticleId());
		article.setCreateTime(LocalDateTime.now());
		article.setUpdateTime(LocalDateTime.now());

		// 保存文章
		Article savedArticle = articleRepository.save(article);
		log.info("文章保存成功，ID: {}", savedArticle.getArticleId());

		// 处理附件
		if (attachment != null && !attachment.isEmpty()) {
			try {
				log.info("开始上传文章附件，文章ID: {}", savedArticle.getArticleId());
				attachmentService.uploadAttachment(attachment, savedArticle);
				savedArticle.setAddAttach(true);
				savedArticle = articleRepository.save(savedArticle);
				log.info("文章附件上传成功");
			}
			catch (IOException e) {
				log.error("文章附件上传失败: {}", e.getMessage());
				throw new FileUploadException("附件上传失败: " + e.getMessage());
			}
		}

		// 处理封面图片
		if (picture != null && !picture.isEmpty()) {
			try {
				log.info("开始上传文章封面图片，文章ID: {}", savedArticle.getArticleId());
				articlePictureService.uploadPicture(picture, savedArticle);
				savedArticle.setAddPicture(true);
				savedArticle = articleRepository.save(savedArticle);
				log.info("文章封面图片上传成功");
			}
			catch (IOException e) {
				log.error("文章封面图片上传失败: {}", e.getMessage());
				throw new FileUploadException("封面图片上传失败: " + e.getMessage());
			}
		}

		return savedArticle;
	}

	/**
	 * 更新文章
	 * @param id 文章ID
	 * @param title 文章标题
	 * @param category 文章分类
	 * @param content 文章内容
	 * @param deleteAttachment 是否删除附件
	 * @param deletePicture 是否删除封面图片
	 * @param attachment 新附件
	 * @param picture 新封面图片
	 * @return 更新后的文章
	 * @throws ResourceNotFoundException 当文章不存在时抛出
	 * @throws FileUploadException 当文件上传失败时抛出
	 */
	@Override
	public Article updateArticle(Long id, String title, String category, String content, boolean deleteAttachment,
			boolean deletePicture, MultipartFile attachment, MultipartFile picture) {
		log.info("更新文章，ID: {}", id);

		// 查找文章
		Article article = getArticleById(id);

		// 更新文章信息
		if (title != null && !title.trim().isEmpty()) {
			article.setTitle(title);
		}
		if (category != null && !category.trim().isEmpty()) {
			article.setCategory(category);
		}
		if (content != null && !content.trim().isEmpty()) {
			article.setContent(content);
		}
		article.setUpdateTime(LocalDateTime.now());

		// 删除附件
		if (deleteAttachment && article.isAddAttach()) {
			log.info("删除文章附件，文章ID: {}", article.getArticleId());
			attachmentService.deleteAttachmentsByArticle(article);
			article.setAddAttach(false);
		}

		// 删除封面图片
		if (deletePicture && article.isAddPicture()) {
			log.info("删除文章封面图片，文章ID: {}", article.getArticleId());
			articlePictureService.deletePictureByArticle(article);
			article.setAddPicture(false);
		}

		// 上传新附件
		if (attachment != null && !attachment.isEmpty()) {
			try {
				log.info("上传新附件，文章ID: {}", article.getArticleId());
				// 如果有旧附件，先删除
				if (article.isAddAttach()) {
					attachmentService.deleteAttachmentsByArticle(article);
				}
				attachmentService.uploadAttachment(attachment, article);
				article.setAddAttach(true);
			}
			catch (IOException e) {
				log.error("附件上传失败: {}", e.getMessage());
				throw new FileUploadException("附件上传失败: " + e.getMessage());
			}
		}

		// 上传新封面图片
		if (picture != null && !picture.isEmpty()) {
			try {
				log.info("上传新封面图片，文章ID: {}", article.getArticleId());
				// 如果有旧图片，先删除
				if (article.isAddPicture()) {
					articlePictureService.deletePictureByArticle(article);
				}
				articlePictureService.uploadPicture(picture, article);
				article.setAddPicture(true);
			}
			catch (IOException e) {
				log.error("封面图片上传失败: {}", e.getMessage());
				throw new FileUploadException("封面图片上传失败: " + e.getMessage());
			}
		}

		// 保存更新后的文章
		Article updatedArticle = articleRepository.save(article);
		log.info("文章更新成功，ID: {}", updatedArticle.getArticleId());
		return updatedArticle;
	}

	/**
	 * 删除文章
	 * @param id 文章ID
	 * @throws ResourceNotFoundException 当文章不存在时抛出
	 */
	@Override
	public void deleteArticle(Long id) {
		log.info("删除文章，ID: {}", id);

		// 查找文章
		Article article = getArticleById(id);

		// 删除相关附件
		if (article.isAddAttach()) {
			log.info("删除文章附件，文章ID: {}", article.getArticleId());
			attachmentService.deleteAttachmentsByArticle(article);
		}

		// 删除相关封面图片
		if (article.isAddPicture()) {
			log.info("删除文章封面图片，文章ID: {}", article.getArticleId());
			articlePictureService.deletePictureByArticle(article);
		}

		// 删除文章
		articleRepository.delete(article);
		log.info("文章删除成功，ID: {}", id);
	}

	/**
	 * 生成唯一的文章编号
	 * @return 文章编号
	 */
	private Long generateArticleId() {
		// 使用UUID的一部分生成唯一编号
		return Math.abs(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
	}

}