package com.website.backend.controller;

import lombok.extern.slf4j.Slf4j;
import com.website.backend.constant.HttpStatusConstants;
import com.website.backend.model.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.website.backend.entity.Visitor;
import com.website.backend.repository.VisitorRepository;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class HomeController {
    private final VisitorRepository visitorRepo;


    public HomeController(VisitorRepository visitorRepo) {
        this.visitorRepo = visitorRepo;
    }

    @GetMapping("/")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("visitorCount", visitorRepo.count());
        return ApiResponse.success(response);
    }

    @GetMapping("/guestbook")
    public ApiResponse<Map<String, Object>> guestbook() {
        // 统计留言数量
        Map<String, Object> response = new HashMap<>();
        response.put("messageCount", visitorRepo.count());
        response.put("messages", visitorRepo.findAll());
        return ApiResponse.success(response);
    }

    @PostMapping("/guestbook/add")
    public ApiResponse<Map<String, Object>> submitMessage(
            @RequestParam String message,
            HttpServletRequest request) {
        log.info("收到留言提交请求，IP地址: {}", request.getRemoteAddr());
        Visitor visitor = new Visitor();
        visitor.setObserveTime(LocalDateTime.now());
        // 生成固定前缀+UUID的昵称
        String nickname = "Bro有话说" + UUID.randomUUID().toString().substring(0, 8);
        visitor.setNickname(nickname);
        visitor.setMessage(message);
        visitor.setIpAddress(request.getRemoteAddr());

        visitorRepo.save(visitor);
        log.info("留言添加成功，昵称: {}", nickname);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "留言添加成功");
        response.put("nickname", nickname);
        return ApiResponse.success(response);
    }

    // 管理员更新留言
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/guestbook/{id}")
    public ApiResponse<Map<String, Object>> updateMessage(
            @PathVariable Long id,
            @RequestParam String message) {
        log.info("收到更新留言请求，留言ID: {}", id);
        try {
            Optional<Visitor> visitorOptional = visitorRepo.findById(id);
            if (visitorOptional.isEmpty()) {
                log.warn("留言不存在，ID: {}", id);
                return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, "留言不存在");
            }

            Visitor visitor = visitorOptional.get();
            visitor.setMessage(message);
            visitor.setObserveTime(LocalDateTime.now());
            Visitor updatedVisitor = visitorRepo.save(visitor);
            log.info("留言更新成功，ID: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "留言更新成功");
            response.put("updatedMessage", updatedVisitor);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("留言更新失败，ID: {}", id, e);
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "留言更新失败: " + e.getMessage());
        }
    }

    // 管理员删除留言
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/guestbook/{id}")
    public ApiResponse<Map<String, Object>> deleteMessage(@PathVariable Long id) {
        log.info("收到删除留言请求，留言ID: {}", id);
        try {
            Optional<Visitor> visitorOptional = visitorRepo.findById(id);
            if (visitorOptional.isEmpty()) {
                log.warn("留言不存在，ID: {}", id);
                return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, "留言不存在");
            }

            visitorRepo.deleteById(id);
            log.info("留言删除成功，ID: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "留言删除成功");
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("留言删除失败，ID: {}", id, e);
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "留言删除失败: " + e.getMessage());
        }
    }

}
