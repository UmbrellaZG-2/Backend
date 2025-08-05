package com.website.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import com.website.backend.constant.HttpStatusConstants;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.website.backend.entity.Article;
import com.website.backend.entity.Attachment;
import com.website.backend.entity.ArticlePicture;
import com.website.backend.entity.Comment;
import com.website.backend.entity.Tag;
import com.website.backend.entity.ArticleTag;
import com.website.backend.model.ApiResponse;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.repository.AttachmentRepository;
import com.website.backend.repository.ArticlePictureRepository;
import com.website.backend.repository.CommentRepository;
import com.website.backend.repository.TagRepository;
import com.website.backend.repository.ArticleTagRepository;
import com.website.backend.service.AttachmentService;
import com.website.backend.DTO.ArticleDTO;
import com.website.backend.DTO.ArticleListDTO;
import com.website.backend.DTO.DeleteArticleResponseDTO;
import com.website.backend.util.DTOConverter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.website.backend.service.ArticlePictureService;
import com.website.backend.exception.ResourceNotFoundException;
import com.website.backend.exception.FileUploadException;

import java.io.IOException;
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
    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;
    private final ArticlePictureService articlePictureService;
    private final ArticlePictureRepository articlePictureRepository;
    private final DTOConverter dtoConverter;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    public ArticleController(ArticleRepository articleRepo, AttachmentService attachmentService, AttachmentRepository attachmentRepository, ArticlePictureService articlePictureService, ArticlePictureRepository articlePictureRepository, DTOConverter dtoConverter, CommentRepository commentRepository, TagRepository tagRepository, ArticleTagRepository articleTagRepository) {
        this.articleRepo = articleRepo;
        this.attachmentService = attachmentService;
        this.attachmentRepository = attachmentRepository;
        this.articlePictureService = articlePictureService;
        this.articlePictureRepository = articlePictureRepository;
        this.dtoConverter = dtoConverter;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
        this.articleTagRepository = articleTagRepository;

    }

    // 抽取公共方法处理分页和DTO转换
    private ArticleListDTO buildArticleListDTO(Page<Article> articlePage) {
        List<ArticleDTO> articleDTOList = articlePage.getContent().stream()
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

    @GetMapping({"","/"})
    //文章列表页 - 分页加载，返回的文章对象包含addPicture字段，前端可根据该字段决定是否显示封面图片
    public ApiResponse<ArticleListDTO> articles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            logger.info("获取文章列表，页码: {}, 每页数量: {}", page, size);
            // 使用分页查询
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> articlePage = articleRepo.findAll(pageable);
            ArticleListDTO articleListDTO = buildArticleListDTO(articlePage);
            logger.info("成功获取文章列表，共 {} 页，当前第 {} 页", articleListDTO.getTotalPages(), articleListDTO.getCurrentPage());
            return ApiResponse.success(articleListDTO);
    }


    // 文章详情页
    @GetMapping("/{id}")
    public ApiResponse<ArticleDTO> articleDetails(@PathVariable Long id) {
        logger.info("获取文章详情，ID: {}", id);
        Article article = articleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

        // 将Article转换为ArticleDTO
        ArticleDTO dto = dtoConverter.convertToDTO(article);
        logger.info("成功获取文章详情，标题: {}", dto.getTitle());
        return ApiResponse.success(dto);
    }


    //文章分类页
    @GetMapping("/category/{category}")
    public ApiResponse<ArticleListDTO> articlesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            logger.info("获取分类 [{}] 文章列表，页码: {}, 每页数量: {}", category, page, size);
            // 使用分页查询按分类获取文章
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> articlePage = articleRepo.findByCategory(category, pageable);
            ArticleListDTO articleListDTO = buildArticleListDTO(articlePage);
            logger.info("成功获取分类 [{}] 文章列表，共 {} 页，当前第 {} 页", category, articleListDTO.getTotalPages(), articleListDTO.getCurrentPage());
            return ApiResponse.success(articleListDTO);
    }

    // 管理员创建文章（支持附件上传和封面图片上传）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ApiResponse<ArticleDTO> createArticle(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile attachment,
            @RequestParam(required = false) MultipartFile picture) {
            logger.info("创建新文章，标题: {}", title);
            Article article = new Article();
            article.setTitle(title);
            article.setCategory(category);
            article.setContent(content);

            // 保存文章到数据库以获取自增id
            Article savedArticle = articleRepo.save(article);
            logger.info("文章保存成功，ID: {}", savedArticle.getArticleId());

            // 上传附件（如果有）
            if (attachment != null && !attachment.isEmpty()) {
                logger.info("开始上传文章附件，文章ID: {}", savedArticle.getArticleId());
                try {
                    attachmentService.uploadAttachment(attachment, savedArticle);
                }
           catch (IOException e) {
                    logger.error("文章附件上传失败: {}", e.getMessage());
                    throw new FileUploadException(savedArticle.getTitle() + "文章创建成功，但附件上传失败: " + e.getMessage());
                }
                logger.info("文章附件上传成功");
            }

            // 上传封面图片（如果有）
            if (picture != null && !picture.isEmpty()) {
                logger.info("开始上传文章封面图片，文章ID: {}", savedArticle.getArticleId());
                try {
                    articlePictureService.uploadPicture(picture, savedArticle);
                    logger.info("文章封面图片上传成功");
                } catch (IOException e) {
                    // 抛出文件上传异常
                    logger.error("文章封面图片上传失败: {}", e.getMessage());
                    throw new FileUploadException(savedArticle.getTitle() + "文章创建成功，但图片上传失败: " + e.getMessage());
                }
            }

            // 将Article转换为ArticleDTO
            ArticleDTO dto = dtoConverter.convertToDTO(savedArticle);
            logger.info("文章创建完成，返回DTO: {}", dto.getTitle());
            return ApiResponse.success(dto);
    }

    // 管理员更新文章（支持附件更新和删除，封面图片更新和删除）
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    public ApiResponse<ArticleDTO> updateArticle(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile attachment,
            @RequestParam(required = false, defaultValue = "false") boolean deleteAttachment,
            @RequestParam(required = false) MultipartFile picture,
            @RequestParam(required = false, defaultValue = "false") boolean deletePicture) {
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

            // 处理附件
            if (deleteAttachment) {
                logger.info("删除文章附件，文章ID: {}", article.getArticleId());
                attachmentService.deleteAttachmentsByArticle(article);
                article.setAddAttach(false);
                logger.info("文章附件删除成功");
            } else if (attachment != null && !attachment.isEmpty()) {
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
            } else if (picture != null && !picture.isEmpty()) {
                // 如果已有图片，先删除
                if (article.isAddPicture()) {
                    logger.info("删除旧文章封面图片，文章ID: {}", article.getArticleId());
                    articlePictureService.deletePictureByArticle(article);
                }
                article.setAddPicture(true);
                logger.info("准备上传新文章封面图片");
            } // 否则保持原有状态

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

            // 上传附件（如果有）
            if (attachment != null && !attachment.isEmpty()) {
                logger.info("开始上传文章附件，文章ID: {}", updatedArticle.getArticleId());
                Attachment newAttachment = attachmentService.uploadAttachment(attachment, updatedArticle);
                newAttachment.setArticleId(updatedArticle.getArticleId());
                attachmentRepository.save(newAttachment);
                logger.info("文章附件上传成功");
            }

            // 上传封面图片（如果有）
            if (picture != null && !picture.isEmpty()) {
                logger.info("开始上传文章封面图片，文章ID: {}", updatedArticle.getArticleId());
                try {
                    ArticlePicture newPicture = articlePictureService.uploadPicture(picture, updatedArticle);
                    newPicture.setArticleId(updatedArticle.getArticleId());
                    articlePictureRepository.save(newPicture);
                    logger.info("文章封面图片上传成功");
                } catch (IOException e) {
                    // 处理图片上传异常
                    logger.error("文章封面图片上传失败: {}", e.getMessage());
                    return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, updatedArticle.getTitle()+" 文章更新成功，但图片上传失败: " + e.getMessage());
                }
            }

            // 将Article转换为ArticleDTO
            ArticleDTO dto = dtoConverter.convertToDTO(updatedArticle);
            logger.info("文章更新完成，返回DTO: {}", dto.getTitle());
            return ApiResponse.success(dto);
        } catch (Exception e) {
            logger.error("文章更新失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章更新失败: " + e.getMessage());
        }
    }

    // 下载附件接口 - 所有用户可访问
    @GetMapping("/attachments/{attachmentId}")
    public void downloadAttachment(@PathVariable Long attachmentId, HttpServletResponse response) {
        try {
            Attachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new IOException("Attachment not found"));
            byte[] fileContent = attachmentService.downloadAttachment(attachmentId);

            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"");
            response.setContentLength(fileContent.length);
            
            // 写入响应体
            try (java.io.OutputStream os = response.getOutputStream()) {
                os.write(fileContent);
                os.flush();
            }
        } catch (IOException e) {
            response.setStatus(HttpStatusConstants.NOT_FOUND);
            try {
                response.getWriter().write("附件不存在: " + e.getMessage());
            } catch (IOException ex) {
                // 忽略
            }
        } catch (Exception e) {
            response.setStatus(HttpStatusConstants.INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("下载附件失败: " + e.getMessage());
            } catch (IOException ex) {
                // 忽略
            }
        }
    }


    // 管理员删除文章（同时删除相关附件）
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ApiResponse<DeleteArticleResponseDTO> deleteArticle(@PathVariable Long id) {
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

            // 删除文章
            articleRepo.deleteById(id);

            DeleteArticleResponseDTO response = new DeleteArticleResponseDTO();
            response.setSuccess(true);
            response.setMessage("文章删除成功");
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章删除失败: " + e.getMessage());
        }
    }

    // 添加评论
    @PostMapping("/{articleId}/comments")
    public ApiResponse<Comment> addComment(
            @PathVariable Long articleId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            HttpServletRequest request) {
        logger.info("添加评论，文章ID: {}", articleId);
        try {
            // 检查文章是否存在
            Article article = articleRepo.findById(articleId)
                    .orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

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
        } catch (ResourceNotFoundException e) {
            logger.error("添加评论失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
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
            Article article = articleRepo.findById(articleId)
                    .orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

            List<Comment> comments = commentRepository.findByArticleId(articleId);
            logger.info("成功获取文章评论，数量: {}", comments.size());
            return ApiResponse.success(comments);
        } catch (ResourceNotFoundException e) {
            logger.error("获取文章评论失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("获取文章评论失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "获取文章评论失败: " + e.getMessage());
        }
    }

    // 为文章添加标签
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{articleId}/tags")
    public ApiResponse<List<Tag>> addArticleTags(
            @PathVariable Long articleId,
            @RequestParam List<String> tagNames) {
        logger.info("为文章添加标签，文章ID: {}, 标签数量: {}", articleId, tagNames.size());
        try {
            // 检查文章是否存在
            Article article = articleRepo.findById(articleId)
                    .orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

            List<Tag> tags = new ArrayList<>();
            for (String tagName : tagNames) {
                // 查找或创建标签
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setCreateTime(LocalDateTime.now());
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }

            logger.info("文章标签添加成功");
            return ApiResponse.success(tags);
        } catch (ResourceNotFoundException e) {
            logger.error("添加文章标签失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("添加文章标签失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "添加文章标签失败: " + e.getMessage());
        }
    }

    // 获取文章的所有标签
    @GetMapping("/{articleId}/tags")
    public ApiResponse<List<Tag>> getArticleTags(@PathVariable Long articleId) {
        logger.info("获取文章标签，文章ID: {}", articleId);
        try {
            // 检查文章是否存在
            Article article = articleRepo.findById(articleId)
                    .orElseThrow(() -> new ResourceNotFoundException("文章不存在"));

            // 通过article_tags关联表查询标签ID
            List<ArticleTag> articleTags = articleTagRepository.findByArticleId(articleId);
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .collect(Collectors.toList());

            // 根据标签ID查询标签信息
            List<Tag> tags = new ArrayList<>();
            if (!tagIds.isEmpty()) {
                tags = tagRepository.findAllById(tagIds);
            }

            logger.info("成功获取文章标签，数量: {}", tags.size());
            return ApiResponse.success(tags);
        } catch (ResourceNotFoundException e) {
            logger.error("获取文章标签失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
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
        } catch (Exception e) {
            logger.error("获取所有标签失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "获取所有标签失败: " + e.getMessage());
        }
    }

    // 根据标签获取文章列表
    @GetMapping("/tag/{tagName}")
    public ApiResponse<ArticleListDTO> articlesByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("获取标签 [{}] 文章列表，页码: {}, 每页数量: {}", tagName, page, size);
        try {
            // 查找标签
            Tag tag = tagRepository.findByName(tagName)
                    .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));

            // 通过article_tags关联表查询文章ID
            List<ArticleTag> articleTags = articleTagRepository.findByTagId(tag.getId());
            List<Long> articleIds = articleTags.stream()
                    .map(ArticleTag::getArticleId)
                    .collect(Collectors.toList());

            // 根据文章ID查询文章信息并分页
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> articlePage;
            if (articleIds.isEmpty()) {
                articlePage = Page.empty(pageable);
            } else {
                articlePage = articleRepo.findByIdIn(new ArrayList<>(articleIds), pageable);
            }

            ArticleListDTO articleListDTO = buildArticleListDTO(articlePage);
            logger.info("成功获取标签 [{}] 文章列表，共 {} 页，当前第 {} 页", tagName, articleListDTO.getTotalPages(), articleListDTO.getCurrentPage());
            return ApiResponse.success(articleListDTO);
        } catch (ResourceNotFoundException e) {
            logger.error("获取标签文章列表失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("获取标签文章列表失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "获取标签文章列表失败: " + e.getMessage());
        }
    }

}
