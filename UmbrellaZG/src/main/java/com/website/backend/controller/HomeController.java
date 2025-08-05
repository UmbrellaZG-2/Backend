package com.website.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.website.backend.entity.Role;
import com.website.backend.entity.User;
import com.website.backend.exception.ResourceNotFoundException;
import com.website.backend.model.ApiResponse;
import com.website.backend.repository.RoleRepository;
import com.website.backend.repository.UserRepository;
import com.website.backend.security.JwtTokenProvider;
import lombok.Data;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public HomeController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 首页重定向
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> home() {
        return ResponseEntity.ok(ApiResponse.success("请选择登录方式", null));
    }

    // 管理员登录
    @PostMapping("/api/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");

        return ResponseEntity.ok(response);
    }

    // 游客登录
    @PostMapping("/api/guest/login")
    public ResponseEntity<?> guestLogin() {
        // 创建临时游客用户
        String guestUsername = "guest_" + System.currentTimeMillis();
        String guestPassword = "guest_password";

        User guestUser = new User();
        guestUser.setUsername(guestUsername);
        guestUser.setPassword(passwordEncoder.encode(guestPassword));

        Role visitorRole = roleRepository.findByName(Role.RoleName.ROLE_VISITOR)
                .orElseThrow(() -> new ResourceNotFoundException("游客角色不存在"));
        guestUser.setRoles(Collections.singleton(visitorRole));

        userRepository.save(guestUser);

        // 自动登录游客用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        guestUsername,
                        guestPassword
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("message", "游客登录成功");

        return ResponseEntity.ok(response);
    }

    // 登录请求体
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}