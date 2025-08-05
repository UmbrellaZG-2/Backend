package com.website.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Table(name = "articles")
@Entity
@Data
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(nullable = false, unique = true)
    private Long articleId; // 文章编号，用于业务标识

    @Column(length = 100)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    private String category;
    private boolean addAttach = false; // 默认为false，不带附件
    private boolean addPicture = false; // 默认为false，不带封面图片

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Article() {}

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
