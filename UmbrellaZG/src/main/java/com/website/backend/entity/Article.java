package com.website.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类，对应文章表
 */
@Table(name = "articles")
@Entity
@Data
public class Article {

	/** 主键ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 文章编号，用于业务标识，唯一 */
	@Column(nullable = false, unique = true)
	private Long articleId;

	/** 文章标题 */
	@Column(length = 100)
	private String title;

	/** 文章内容 */
	@Lob
	@Column(columnDefinition = "TEXT")
	private String content;

	/** 文章分类 */
	private String category;

	/** 是否有附件 */
	private boolean addAttach = false;

	/** 是否有封面图片 */
	private boolean addPicture = false;

	/** 创建时间 */
	private LocalDateTime createTime;

	/** 更新时间 */
	private LocalDateTime updateTime;

	public Article() {
	}

	/**
	 * 新建时自动设置创建和更新时间
	 */
	@PrePersist
	protected void onCreate() {
		createTime = LocalDateTime.now();
		updateTime = LocalDateTime.now();
	}

	/**
	 * 更新时自动设置更新时间
	 */
	@PreUpdate
	protected void onUpdate() {
		updateTime = LocalDateTime.now();
	}

}
