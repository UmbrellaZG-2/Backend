package com.website.backend.DTO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章数据传输对象 用于封装文章相关数据在前后端之间传输
 */
@Data
public class ArticleDTO {

	private Long id;

	private String title;

	private String content;

	private String category;

	private boolean hasAttachment;

	private boolean hasCoverImage;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

}