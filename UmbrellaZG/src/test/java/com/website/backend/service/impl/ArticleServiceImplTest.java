package com.website.backend.service.impl;

import com.website.backend.entity.Article;
import com.website.backend.exception.ResourceNotFoundException;
import com.website.backend.repository.ArticleRepository;
import com.website.backend.service.AttachmentService;
import com.website.backend.service.ArticlePictureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private ArticlePictureService articlePictureService;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Article testArticle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
    void testGetAllArticles() {
        // 准备测试数据
        List<Article> articles = Arrays.asList(testArticle);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        // 配置模拟行为
        when(articleRepository.findAll(pageable)).thenReturn(articlePage);

        // 执行测试
        Page<Article> result = articleService.getAllArticles(pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testArticle.getTitle(), result.getContent().get(0).getTitle());

        // 验证交互
        verify(articleRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetArticlesByCategory() {
        // 准备测试数据
        String category = "测试分类";
        List<Article> articles = Arrays.asList(testArticle);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        // 配置模拟行为
        when(articleRepository.findByCategory(category, pageable)).thenReturn(articlePage);

        // 执行测试
        Page<Article> result = articleService.getArticlesByCategory(category, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(category, result.getContent().get(0).getCategory());

        // 验证交互
        verify(articleRepository, times(1)).findByCategory(category, pageable);
    }

    @Test
    void testGetArticleByIdFound() {
        // 配置模拟行为
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));

        // 执行测试
        Article result = articleService.getArticleById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testArticle.getId(), result.getId());

        // 验证交互
        verify(articleRepository, times(1)).findById(1L);
    }

    @Test
    void testGetArticleByIdNotFound() {
        // 配置模拟行为
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            articleService.getArticleById(999L);
        });

        // 验证交互
        verify(articleRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateArticle() throws IOException {
        // 准备测试数据
        String title = "新文章";
        String category = "新技术";
        String content = "新内容";
        MultipartFile attachment = mock(MultipartFile.class);
        MultipartFile picture = mock(MultipartFile.class);

        // 配置模拟行为
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);
        when(attachment.isEmpty()).thenReturn(false);
        when(picture.isEmpty()).thenReturn(false);

        // 执行测试
        Article result = articleService.createArticle(title, category, content, attachment, picture);

        // 验证结果
        assertNotNull(result);
        assertEquals(testArticle.getId(), result.getId());

        // 验证交互
        verify(articleRepository, times(2)).save(any(Article.class));
        verify(attachmentService, times(1)).uploadAttachment(attachment, testArticle);
        verify(articlePictureService, times(1)).uploadPicture(picture, testArticle);
    }

    @Test
    void testUpdateArticle() throws IOException {
        // 准备测试数据
        Long id = 1L;
        String newTitle = "更新后的标题";
        MultipartFile newAttachment = mock(MultipartFile.class);

        // 配置模拟行为
        when(articleRepository.findById(id)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);
        when(newAttachment.isEmpty()).thenReturn(false);

        // 执行测试
        Article result = articleService.updateArticle(id, newTitle, null, null, false, false, newAttachment, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());

        // 验证交互
        verify(articleRepository, times(1)).findById(id);
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(attachmentService, times(1)).uploadAttachment(newAttachment, testArticle);
    }

    @Test
    void testDeleteArticle() {
        // 配置模拟行为
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        doNothing().when(articleRepository).delete(testArticle);

        // 执行测试
        articleService.deleteArticle(1L);

        // 验证交互
        verify(articleRepository, times(1)).findById(1L);
        verify(articleRepository, times(1)).delete(testArticle);
    }

    // 测试私有方法generateArticleId()
    @Test
    void testGenerateArticleId() {
        // 使用反射调用私有方法
        try {
            java.lang.reflect.Method method = ArticleServiceImpl.class.getDeclaredMethod("generateArticleId");
            method.setAccessible(true);
            Long result = (Long) method.invoke(articleService);
            assertNotNull(result);
            assertTrue(result > 0);
        } catch (Exception e) {
            fail("反射调用generateArticleId方法失败: " + e.getMessage());
        }
    }
}