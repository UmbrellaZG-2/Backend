package com.website.backend.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
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
import com.website.backend.service.GuestService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证控制器 处理所有认证相关的API请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final JwtTokenProvider jwtTokenProvider;

	private final GuestService guestService;

	/**
	 * 构造函数注入依赖
	 * @param authenticationManager 身份验证管理器
	 * @param jwtTokenProvider JWT令牌提供器
	 * @param guestService 游客服务
	 */
	public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
			GuestService guestService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.guestService = guestService;
	}

	/**
	 * 管理员登录
	 */
	@PostMapping("/admin/login")
	public ApiResponse<Map<String, Object>> adminLogin(@RequestBody LoginRequest loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();
		log.info("管理员登录请求: 用户名={}", username);
		try {
			// 进行身份验证
			Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			// 设置身份验证上下文
			SecurityContextHolder.getContext().setAuthentication(authentication);

			// 检查用户是否具有管理员角色
			boolean isAdmin = authentication.getAuthorities()
				.stream()
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
		}
		catch (AuthenticationException e) {
			log.error("管理员登录失败: 用户名={}, 错误={}", username, e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.UNAUTHORIZED, "认证失败: 用户名或密码错误");
		}
		catch (ResourceNotFoundException e) {
			log.error("管理员登录失败: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.FORBIDDEN, e.getMessage());
		}
		catch (Exception e) {
			log.error("管理员登录发生未知错误: {}", e.getMessage());
			return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "登录失败: 服务器内部错误");
		}
	}

	/**
	 * 游客登录
	 */
	@PostMapping("/guest/login")
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

}