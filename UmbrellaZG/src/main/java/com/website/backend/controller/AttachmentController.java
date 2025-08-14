package com.website.backend.controller;

import com.website.backend.constant.HttpStatusConstants;
import com.website.backend.entity.Attachment;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.repository.AttachmentRepository;
import com.website.backend.repository.ArticlePictureRepository;
import com.website.backend.service.AttachmentService;
import com.website.backend.service.ArticlePictureService;
import com.website.backend.util.DTOConverter;
import com.website.backend.service.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;

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

	@Autowired
	private RateLimitService rateLimitService;

	// 下载附件接口 - 所有用户可访问
	@GetMapping("/{attachmentId}")
	public void downloadAttachment(@PathVariable Long attachmentId, HttpServletRequest request,
			HttpServletResponse response) {
		// 获取客户端IP地址
		String clientIp = getClientIpAddress(request);

		// 检查IP是否被阻止或超过下载限制
		if (rateLimitService.isIpBlocked(clientIp) || rateLimitService.recordDownloadRequest(clientIp)) {
			response.setStatus(HttpStatusConstants.FORBIDDEN);
			try {
				response.getWriter().write("下载频率过高，请24小时后再试");
			}
			catch (IOException ex) {
				// 忽略
			}
			return;
		}
		try {
			Attachment attachment = attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new IOException("Attachment not found"));
			byte[] fileContent = attachmentService.downloadAttachment(attachmentId);

			// 设置响应头
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + attachment.getFileName() + "\"");
			response.setContentLength(fileContent.length);

			// 写入响应体
			try (OutputStream os = response.getOutputStream()) {
				os.write(fileContent);
				os.flush();
			}
		}
		catch (IOException e) {
			response.setStatus(HttpStatusConstants.NOT_FOUND);
			try {
				response.getWriter().write("附件不存在: " + e.getMessage());
			}
			catch (IOException ex) {
				// 忽略
			}
		}
		catch (Exception e) {
			response.setStatus(HttpStatusConstants.INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("下载附件失败: " + e.getMessage());
			}
			catch (IOException ex) {
				// 忽略
			}
		}
	}

	// 获取客户端IP地址
	private String getClientIpAddress(HttpServletRequest request) {
		String xForwardedForHeader = request.getHeader("X-Forwarded-For");
		if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
			return xForwardedForHeader.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

}