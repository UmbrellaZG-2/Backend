package com.website.backend.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.website.backend.exception.ResourceNotFoundException;
import com.website.backend.model.ApiResponse;
import com.website.backend.security.JwtTokenProvider;
import com.website.backend.constant.HttpStatusConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理员控制器
 * 处理管理员相关的API请求
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 构造函数注入依赖
     * @param authenticationManager 身份验证管理器
     * @param jwtTokenProvider JWT令牌提供器
     */
    public AdminController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody HomeController.LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        log.info("管理员登录请求: 用户名={}", username);
        try {
            // 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 设置身份验证上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 检查用户是否具有管理员角色
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                log.warn("用户 {} 没有管理员权限", username);
                throw new ResourceNotFoundException("用户 " + username + " 没有管理员权限");
            }

            // 生成JWT令牌
            String jwt = jwtTokenProvider.generateToken(authentication);
            log.info("管理员 {} 登录成功", username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", jwt);
            return ApiResponse.success(response);
        } catch (AuthenticationException e) {
            log.error("管理员登录失败: 用户名={}, 错误={}", username, e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.UNAUTHORIZED, "认证失败: 用户名或密码错误");
        } catch (ResourceNotFoundException e) {
            log.error("管理员登录失败: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("管理员登录发生未知错误: {}", e.getMessage());
            return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "登录失败: 服务器内部错误");
        }
    }
}