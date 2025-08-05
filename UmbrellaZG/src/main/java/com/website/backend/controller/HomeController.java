package com.website.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.website.backend.model.ApiResponse;
import com.website.backend.service.GuestService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@Slf4j
public class HomeController {

    private final GuestService guestService;

    public HomeController(
            PasswordEncoder passwordEncoder,
            GuestService guestService) {
        this.guestService = guestService;
    }

    // 首页重定向
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> home() {
        return ResponseEntity.ok(ApiResponse.success("请选择登录方式", null));
    }

    // 游客登录
    @PostMapping("/api/guest/login")
    public ResponseEntity<?> guestLogin() {
        log.info("处理游客登录请求");

        // 生成游客用户名和密码
        String guestUsername = guestService.generateGuestUsername();
        String guestPassword = "guest_password";

        // 保存游客信息到Redis
        guestService.saveGuestToRedis(guestUsername, guestPassword);

        // 生成JWT令牌
        String jwt = guestService.generateGuestToken(guestUsername);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("message", "游客登录成功，有效期6小时");

        return ResponseEntity.ok(response);
    }

    // 登录请求体
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    // 跳转到关于我页面
    @GetMapping("/aboutMe")
    public ResponseEntity<Void> redirectToAboutMe() {
        log.info("重定向到关于我页面");
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/aboutMe.html")
                .build();
    }
}