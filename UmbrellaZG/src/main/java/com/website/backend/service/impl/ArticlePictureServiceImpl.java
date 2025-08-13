package com.website.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.website.backend.entity.Article;
import com.website.backend.entity.ArticlePicture;
import com.website.backend.repository.ArticlePictureRepository;
import com.website.backend.service.ArticlePictureService;
import com.website.backend.config.FileStorageConfig;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class ArticlePictureServiceImpl implements ArticlePictureService {

	private final ArticlePictureRepository articlePictureRepository;

	private final FileStorageConfig fileStorageConfig;

	public ArticlePictureServiceImpl(ArticlePictureRepository articlePictureRepository,
			FileStorageConfig fileStorageConfig) {
		this.articlePictureRepository = articlePictureRepository;
		this.fileStorageConfig = fileStorageConfig;
	}

	@Override
	public ArticlePicture uploadPicture(MultipartFile file, Article article) throws IOException {
		// 验证图片格式
		String fileName = file.getOriginalFilename();
		String contentType = file.getContentType();

		if (fileName == null || !fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".png")) {
			throw new IOException("只支持JPG和PNG格式的图片");
		}

		if (contentType == null || !contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
			throw new IOException("文件内容类型不符合要求，只支持JPG和PNG格式的图片");
		}

		// 创建图片信息实体
		ArticlePicture picture = new ArticlePicture();
		picture.setFileName(file.getOriginalFilename());
		picture.setFileType(file.getContentType());
		picture.setFileSize(file.getSize());
		picture.setUploadTime(LocalDateTime.now());
		picture.setArticle(article);

		// 保存图片信息到数据库
		ArticlePicture savedPicture = articlePictureRepository.save(picture);

		// 获取文件存储路径
		String storagePath = fileStorageConfig.getArticlePictureStoragePath();

		// 生成唯一文件名
		String originalFilename = file.getOriginalFilename();
		String cleanedFileName = originalFilename != null ? StringUtils.cleanPath(originalFilename) : "";
		String uniqueFileName = savedPicture.getPictureId() + "_" + cleanedFileName;
		String filePath = storagePath + File.separator + uniqueFileName;

		// 保存文件到本地文件系统
		try {
			File destinationFile = new File(filePath);
			file.transferTo(destinationFile);
			log.info("Image uploaded to file system: {}", filePath);

			// 更新图片实体中的文件路径
			savedPicture.setFilePath(filePath);
			return articlePictureRepository.save(savedPicture);
		}
		catch (IOException e) {
			log.error("Failed to upload image to file system: {}", e.getMessage());
			throw new IOException("Could not upload image file", e);
		}
	}

	@Override
	public byte[] downloadPicture(Long pictureId) throws IOException {
		// 从数据库获取图片信息
		Optional<ArticlePicture> pictureOptional = articlePictureRepository.findById(pictureId);
		if (pictureOptional.isEmpty()) {
			throw new IOException("Picture not found with id: " + pictureId);
		}

		ArticlePicture picture = pictureOptional.get();
		String filePath = picture.getFilePath();

		if (filePath == null || filePath.isEmpty()) {
			throw new IOException("Picture file path not found for id: " + pictureId);
		}

		// 从文件系统读取图片内容
		File file = new File(filePath);
		if (!file.exists()) {
			throw new IOException("Picture file not found: " + filePath);
		}

		return Files.readAllBytes(file.toPath());
	}

	@Override
	public void deletePicture(Long pictureId) {
		// 从数据库获取图片信息
		Optional<ArticlePicture> pictureOptional = articlePictureRepository.findById(pictureId);
		if (pictureOptional.isPresent()) {
			ArticlePicture picture = pictureOptional.get();
			String filePath = picture.getFilePath();

			// 从文件系统删除图片
			if (filePath != null && !filePath.isEmpty()) {
				File file = new File(filePath);
				if (file.exists()) {
					boolean deleted = file.delete();
					if (deleted) {
						log.info("Image deleted from file system: {}", filePath);
					}
					else {
						log.warn("Failed to delete image from file system: {}", filePath);
					}
				}
			}

			// 从数据库删除图片信息
			articlePictureRepository.deleteById(pictureId);
			log.info("Image information deleted from database: {}", pictureId);
		}
		else {
			log.warn("Picture not found for deletion: {}", pictureId);
		}
	}

	@Override
	public void deletePictureByArticle(Article article) {
		// 从数据库获取图片信息
		ArticlePicture picture = getPictureByArticle(article);
		if (picture != null) {
			String filePath = picture.getFilePath();

			// 从文件系统删除图片
			if (filePath != null && !filePath.isEmpty()) {
				File file = new File(filePath);
				if (file.exists()) {
					boolean deleted = file.delete();
					if (deleted) {
						log.info("Image deleted from file system: {}", filePath);
					}
					else {
						log.warn("Failed to delete image from file system: {}", filePath);
					}
				}
			}

			// 从数据库删除图片信息
			articlePictureRepository.deleteByArticle(article);
			log.info("Image information deleted from database for article: {}", article.getArticleId());
		}
		else {
			log.info("No picture found for article: {}", article.getArticleId());
		}
	}

	@Override
	public ArticlePicture getPictureByArticle(Article article) {
		return articlePictureRepository.findByArticle(article).orElse(null);
	}

}