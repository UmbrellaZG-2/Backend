package com.website.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;
import java.io.Serializable;

@Entity
@Table(name = "article_tags")
@Data
public class ArticleTag implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long articleId;

	@Column(nullable = false)
	private Long tagId;

}