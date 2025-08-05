package com.website.backend;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.service.AttachmentService;
import com.website.backend.service.ArticlePictureService;
import com.website.backend.service.impl.ArticleServiceImpl;

@TestConfiguration
public class TestConfig {

    @Bean
    public ArticleRepository articleRepository() {
        return Mockito.mock(ArticleRepository.class);
    }

    @Bean
    public AttachmentService attachmentService() {
        return Mockito.mock(AttachmentService.class);
    }

    @Bean
    public ArticlePictureService articlePictureService() {
        return Mockito.mock(ArticlePictureService.class);
    }

    @Bean
    public ArticleServiceImpl articleService() {
        return new ArticleServiceImpl(
            articleRepository(),
            attachmentService(),
            articlePictureService()
        );
    }
}