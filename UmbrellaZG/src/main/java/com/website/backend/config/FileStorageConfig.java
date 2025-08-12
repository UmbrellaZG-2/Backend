package com.website.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

@Configuration
public class FileStorageConfig {

	// 文件存储路径
	@Value("${file.storage.path}")
	private String fileStoragePath;

	// 获取文件存储目录
	public String getFileStoragePath() {
		// 如果路径不存在，则创建
		try {
			File file = ResourceUtils.getFile(fileStoragePath);
			if (!file.exists()) {
				file.mkdirs();
			}
			return file.getAbsolutePath();
		}
		catch (IOException e) {
			throw new RuntimeException("Could not resolve file storage path", e);
		}
	}

	// 获取文章图片存储目录
	public String getArticlePictureStoragePath() {
		String path = getFileStoragePath() + File.separator + "article_pictures";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	// 获取附件存储目录
	public String getAttachmentStoragePath() {
		String path = getFileStoragePath() + File.separator + "attachments";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

}