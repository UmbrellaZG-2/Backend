package com.website.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "attachments")
@Data
public class Attachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long attachmentId;

	@Column(length = 255, nullable = false)
	private String fileName;

	@Column(length = 255, nullable = false)
	private String filePath;

	@Column(length = 100)
	private String fileType;

	private Long fileSize;

	private LocalDateTime uploadTime;

	@ManyToOne
	@JoinColumn(name = "article_id", nullable = false)
	private Article article;

	public Attachment() {
	}

}