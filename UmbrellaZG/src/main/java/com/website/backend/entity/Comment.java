package com.website.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import java.time.LocalDateTime;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;

/**
 * 评论实体类，对应文章评论表
 */
@Entity
@Data
public class Comment {

	/** 主键ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 所属文章ID */
	@Column(nullable = false)
	private Long articleId;

	/** 父评论ID（顶级评论为null） */
	private Long parentId;

	/** 昵称 */
	@Column(nullable = false, length = 100)
	private String nickname;

	/** 评论内容 */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	/** 创建时间 */
	@Column(nullable = false)
	private LocalDateTime createTime;

	/** 评论者IP地址 */
	@Column(length = 50)
	private String ipAddress;

}