package com.website.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "article_pictures")
@Data
public class ArticlePicture {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pictureId;

	@Column(length = 255, nullable = false)
	private String fileName;

	@Column(length = 255, nullable = false)
	private String filePath;

	@Column(length = 100)
	private String fileType;

	private Long fileSize;

	private LocalDateTime uploadTime;

	@OneToOne
	@JoinColumn(name = "article_id", nullable = false)
	private Article article;

	public ArticlePicture() {
	}

}