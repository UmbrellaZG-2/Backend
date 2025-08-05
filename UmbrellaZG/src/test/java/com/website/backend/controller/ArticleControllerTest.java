package com.website.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.website.backend.entity.Article;
import com.website.backend.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Autowired
    private ObjectMapper objectMapper;

    private Article testArticle;

    @BeforeEach
    void setUp() {
        // 创建测试文章
        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setArticleId(1001L);
        testArticle.setTitle("测试文章");
        testArticle.setCategory("测试分类");
        testArticle.setContent("测试内容");
        testArticle.setCreateTime(LocalDateTime.now());
        testArticle.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void testGetAllArticles() throws Exception {
        // 准备测试数据
        List<Article> articles = Arrays.asList(testArticle);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        // 配置模拟行为
        Mockito.when(articleService.getAllArticles(pageable)).thenReturn(articlePage);

        // 执行测试
        mockMvc.perform(get("/api/articles")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(testArticle.getTitle()))
                .andExpect(jsonPath("$.totalElements").value(1));

        // 验证交互
        Mockito.verify(articleService, Mockito.times(1)).getAllArticles(pageable);
    }

    @Test
    @WithMockUser
    void testGetArticleById() throws Exception {
        // 配置模拟行为
        Mockito.when(articleService.getArticleById(1L)).thenReturn(testArticle);

        // 执行测试
        mockMvc.perform(get("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testArticle.getTitle()))
                .andExpect(jsonPath("$.category").value(testArticle.getCategory()));

        // 验证交互
        Mockito.verify(articleService, Mockito.times(1)).getArticleById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateArticle() throws Exception {
        // 配置模拟行为
        Mockito.when(articleService.createArticle(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(MultipartFile.class),
                Mockito.any(MultipartFile.class)))
                .thenReturn(testArticle);

        // 执行测试
        mockMvc.perform(multipart("/api/admin/articles")
                .file("attachment", "test-attachment.txt".getBytes())
                .file("picture", "test-picture.jpg".getBytes())
                .param("title", "新文章")
                .param("category", "新技术")
                .param("content", "新内容"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testArticle.getTitle()));

        // 验证交互
        Mockito.verify(articleService, Mockito.times(1)).createArticle(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(MultipartFile.class),
                Mockito.any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateArticle() throws Exception {
        // 配置模拟行为
        Mockito.when(articleService.updateArticle(
                Mockito.anyLong(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.anyBoolean(),
                Mockito.any(MultipartFile.class),
                Mockito.any(MultipartFile.class)))
                .thenReturn(testArticle);

        // 执行测试
        mockMvc.perform(multipart("/api/admin/articles/1")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .file("attachment", "updated-attachment.txt".getBytes())
                .param("title", "更新后的标题")
                .param("category", "更新后的分类")
                .param("content", "更新后的内容"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testArticle.getTitle()));

        // 验证交互
        Mockito.verify(articleService, Mockito.times(1)).updateArticle(
                Mockito.anyLong(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.anyBoolean(),
                Mockito.any(MultipartFile.class),
                Mockito.any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteArticle() throws Exception {
        // 配置模拟行为
        Mockito.doNothing().when(articleService).deleteArticle(1L);

        // 执行测试
        mockMvc.perform(delete("/api/admin/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 验证交互
        Mockito.verify(articleService, Mockito.times(1)).deleteArticle(1L);
    }
}