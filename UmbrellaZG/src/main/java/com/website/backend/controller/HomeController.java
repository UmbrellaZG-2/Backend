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

// 跳转到关于我页面
    @GetMapping("/aboutMe")
    public ResponseEntity<Void> redirectToAboutMe() {
        log.info("重定向到关于我页面");
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/aboutMe.html")
                .build();
    }
}