package com.website.backend.controller;

import com.website.backend.model.ApiResponse;
import com.website.backend.constant.HttpStatusConstants;
import com.website.backend.DTO.ArticleDTO;
import com.website.backend.entity.Article;
import com.website.backend.entity.Attachment;
import com.website.backend.entity.ArticlePicture;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.repository.AttachmentRepository;
import com.website.backend.repository.ArticlePictureRepository;
import com.website.backend.service.AttachmentService;
import com.website.backend.service.ArticlePictureService;
import com.website.backend.util.DTOConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);

    @Autowired
    private ArticleRepository articleRepo;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ArticlePictureRepository articlePictureRepository;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private ArticlePictureService articlePictureService;

    @Autowired
    private DTOConverter dtoConverter;

    // 管理员创建文章时上传附件和图片
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/articles/attachments")
    public ApiResponse<ArticleDTO> createArticleWithAttachments(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String content,
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
                    newAttachment.setArticleId(savedArticle.getArticleId());
                    attachmentRepository.save(newAttachment);
                    logger.info("文章附件上传成功");
                } catch (IOException e) {
                    if (e.getMessage().contains("附件数量超过限制")) {
                        logger.warn("文章附件数量超过限制: {}", e.getMessage());
                        return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, e.getMessage());
                    } else {
                        throw e;
                    }
                }
            }

            // 上传封面图片（如果有）
            if (picture != null && !picture.isEmpty()) {
                logger.info("开始上传文章封面图片，文章ID: {}", savedArticle.getArticleId());
                try {
                    ArticlePicture newPicture = articlePictureService.uploadPicture(picture, savedArticle);
                    newPicture.setArticleId(savedArticle.getArticleId());
                    articlePictureRepository.save(newPicture);
                    logger.info("文章封面图片上传成功");
                } catch (IOException e) {
                    // 处理图片上传异常
                    logger.error("文章封面图片上传失败: {}", e.getMessage());
                    return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, savedArticle.getTitle()+" 文章创建成功，但图片上传失败: " + e.getMessage());
                }
            }

            // 将Article转换为ArticleDTO
            ArticleDTO dto = dtoConverter.convertToDTO(savedArticle);
            logger.info("文章创建完成，返回DTO: {}", dto.getTitle());
            return ApiResponse.success(dto);
        } catch (Exception e) {
            logger.error("文章创建失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文章创建失败: " + e.getMessage());
        }
    }

    // 更新文章时处理附件和图片
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/articles/{id}/attachments")
    public ApiResponse<ArticleDTO> updateArticleAttachments(
            @PathVariable Long id,
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

            // 保存更新后的文章
            Article updatedArticle = articleRepo.save(article);
            logger.info("文章保存成功，ID: {}", updatedArticle.getArticleId());

            // 上传附件（如果有）
            if (attachment != null && !attachment.isEmpty()) {
                logger.info("开始上传文章附件，文章ID: {}", updatedArticle.getArticleId());
                try {
                    Attachment newAttachment = attachmentService.uploadAttachment(attachment, updatedArticle);
                    newAttachment.setArticleId(updatedArticle.getArticleId());
                    attachmentRepository.save(newAttachment);
                    logger.info("文章附件上传成功");
                } catch (IOException e) {
                    if (e.getMessage().contains("附件数量超过限制")) {
                        logger.warn("文章附件数量超过限制: {}", e.getMessage());
                        return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, e.getMessage());
                    } else {
                        throw e;
                    }
                }
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
    @GetMapping("/{attachmentId}")
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
            try (OutputStream os = response.getOutputStream()) {
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

    // 删除文章时删除附件和图片
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/articles/{id}/cleanup")
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
        } catch (Exception e) {
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "清理文章附件和图片失败: " + e.getMessage());
        }
    }
}