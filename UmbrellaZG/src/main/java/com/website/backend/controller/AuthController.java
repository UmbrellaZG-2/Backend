package com.website.backend.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.website.backend.security.JwtTokenProvider;
import com.website.backend.service.GuestService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final JwtTokenProvider jwtTokenProvider;

	private final GuestService guestService;

	/**
	 * 构造函数注入依赖
	 */
	public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
			GuestService guestService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.guestService = guestService;
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

}