package com.website.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import java.time.LocalDateTime;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Data;

/**
 * 标签实体类，对应文章标签表
 */
@Entity
@Data
public class Tag {

	/** 主键ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 标签名称，唯一且不超过50字符 */
	@Column(nullable = false, unique = true, length = 50)
	private String name;

	/** 创建时间 */
	@Column(nullable = false)
	private LocalDateTime createTime;

}