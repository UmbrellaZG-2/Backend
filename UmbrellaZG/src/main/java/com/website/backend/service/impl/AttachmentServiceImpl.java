package com.website.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.website.backend.config.FileStorageConfig;
import org.springframework.util.StringUtils;
import java.io.File;
import java.nio.file.Files;
import com.website.backend.entity.Attachment;
import com.website.backend.entity.Article;
import com.website.backend.entity.SystemConfig;
import com.website.backend.repository.AttachmentRepository;
import com.website.backend.repository.SystemConfigRepository;
import com.website.backend.service.AttachmentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Slf4j
@Service
public class AttachmentServiceImpl implements AttachmentService {

	private final AttachmentRepository attachmentRepository;

	private final FileStorageConfig fileStorageConfig;

	private final SystemConfigRepository systemConfigRepository;

	public AttachmentServiceImpl(AttachmentRepository attachmentRepository, FileStorageConfig fileStorageConfig,
			SystemConfigRepository systemConfigRepository) {
		this.attachmentRepository = attachmentRepository;
		this.fileStorageConfig = fileStorageConfig;
		this.systemConfigRepository = systemConfigRepository;
	}

	@Override
	public Attachment uploadAttachment(MultipartFile file, Article article) throws IOException {
		// 检查附件数量限制
		int maxAttachCount = getMaxAttachmentCount();
		int currentAttachCount = getCurrentAttachmentCount(article);

		if (currentAttachCount >= maxAttachCount) {
			log.warn("文章附件数量超过限制，文章ID: {}, 当前数量: {}, 限制数量: {}", article.getArticleId(), currentAttachCount,
					maxAttachCount);
			throw new IOException("附件数量超过限制，最多可上传" + maxAttachCount + "个附件");
		}

		// 验证附件格式
		String fileName = file.getOriginalFilename();

		if (fileName == null) {
			throw new IOException("文件名不能为空");
		}

		String lowerFileName = fileName.toLowerCase();
		if (!lowerFileName.endsWith(".7z") && !lowerFileName.endsWith(".zip") && !lowerFileName.endsWith(".rar")) {
			throw new IOException("只支持7Z、ZIP和RAR格式的压缩文件");
		}

		log.info("开始上传附件,文件名: {}, 文章ID: {}", fileName, article.getArticleId());
		String fileType = file.getContentType();
		Long fileSize = file.getSize();

		// 获取文件存储路径
		String storagePath = fileStorageConfig.getAttachmentStoragePath();

		// 生成唯一文件名
		String uniqueFileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(fileName);
		String filePath = storagePath + File.separator + uniqueFileName;

		// 保存文件到本地文件系统
		try {
			File destinationFile = new File(filePath);
			file.transferTo(destinationFile);
			log.info("附件上传到文件系统成功: {}", filePath);
		}
		catch (IOException e) {
			log.error("附件上传到文件系统失败: {}", e.getMessage());
			throw new IOException("Could not upload attachment file", e);
		}

		// 创建附件实体并保存到数据库
		Attachment attachment = new Attachment();
		attachment.setFileName(fileName);
		attachment.setFilePath(filePath);
		attachment.setFileType(fileType);
		attachment.setFileSize(fileSize);
		attachment.setUploadTime(LocalDateTime.now());
		attachment.setArticle(article);

		Attachment savedAttachment = attachmentRepository.save(attachment);
		log.info("附件信息保存到数据库成功,附件ID: {}", savedAttachment.getAttachmentId());
		return savedAttachment;
	}

	@Override
	public byte[] downloadAttachment(Long attachmentId) throws IOException {
		log.info("开始下载附件,附件ID: {}", attachmentId);
		// 从数据库获取附件信息
		Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(() -> {
			log.error("附件不存在,附件ID: {}", attachmentId);
			return new IOException("Attachment not found");
		});

		String filePath = attachment.getFilePath();
		if (filePath == null || filePath.isEmpty()) {
			throw new IOException("Attachment file path not found for id: " + attachmentId);
		}

		// 从文件系统读取附件内容
		File file = new File(filePath);
		if (!file.exists()) {
			throw new IOException("Attachment file not found: " + filePath);
		}

		return Files.readAllBytes(file.toPath());
	}

	@Override
	public void deleteAttachment(Long attachmentId) {
		log.info("开始删除附件,附件ID: {}", attachmentId);
		// 从数据库获取附件信息
		Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(() -> {
			log.error("附件不存在,附件ID: {}", attachmentId);
			return new RuntimeException("Attachment not found");
		});

		String filePath = attachment.getFilePath();

		// 从文件系统删除附件
		if (filePath != null && !filePath.isEmpty()) {
			File file = new File(filePath);
			if (file.exists()) {
				boolean deleted = file.delete();
				if (deleted) {
					log.info("从文件系统删除附件成功: {}", filePath);
				}
				else {
					log.warn("从文件系统删除附件失败: {}", filePath);
				}
			}
		}

		// 从数据库删除附件记录
		attachmentRepository.delete(attachment);
		log.info("从数据库删除附件记录成功,附件ID: {}", attachmentId);
	}

	private int getMaxAttachmentCount() {
		// 从数据库中获取最大附件数量配置
		Optional<SystemConfig> configOptional = systemConfigRepository.findByConfigKey("MaxAttachCount");
		if (configOptional.isPresent()) {
			try {
				return Integer.parseInt(configOptional.get().getConfigValue());
			}
			catch (NumberFormatException e) {
				log.error("MaxAttachCount配置值不是有效数字: {}", configOptional.get().getConfigValue());
			}
		}
		else {
			log.warn("未找到MaxAttachCount配置，使用默认值10");
		}
		// 默认值
		return 5;
	}

	private int getCurrentAttachmentCount(Article article) {
		// 获取当前文章的附件数量
		return attachmentRepository.countByArticle(article);
	}

	@Override
	public void deleteAttachmentsByArticle(Article article) {
		log.info("开始删除文章相关的所有附件,文章ID: {}", article.getArticleId());
		// 获取文章相关的所有附件
		java.util.List<Attachment> attachments = attachmentRepository.findByArticle(article);
		log.info("找到{}个与文章相关的附件", attachments.size());

		// 从文件系统和数据库删除每个附件
		for (Attachment attachment : attachments) {
			String filePath = attachment.getFilePath();

			// 从文件系统删除附件
			if (filePath != null && !filePath.isEmpty()) {
				File file = new File(filePath);
				if (file.exists()) {
					boolean deleted = file.delete();
					if (deleted) {
						log.info("从文件系统删除附件成功: {}", filePath);
					}
					else {
						log.warn("从文件系统删除附件失败: {}", filePath);
					}
				}
			}

			// 从数据库删除附件记录
			attachmentRepository.delete(attachment);
			log.info("从数据库删除附件记录成功,附件ID: {}", attachment.getAttachmentId());
		}

		log.info("从数据库删除文章相关的所有附件记录成功,文章ID: {}", article.getArticleId());
	}

}