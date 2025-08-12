package com.website.backend.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.website.backend.entity.Role;
import com.website.backend.repository.RoleRepository;
import com.website.backend.security.JwtTokenProvider;
import com.website.backend.service.GuestService;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GuestServiceImpl implements GuestService {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private RoleRepository roleRepository;

	@Value("${app.guest.expirationMs}")
	private long guestExpirationMs;

	private static final String GUEST_PREFIX = "guest_";

	private static final String REDIS_KEY_PREFIX = "guest:";

	@Override
	public String generateGuestUsername() {
		return GUEST_PREFIX + UUID.randomUUID();
	}

	@Override
	public void saveGuestToRedis(String username, String password) {
		log.info("保存游客信息到Redis: {}", username);

		// 获取游客角色
		Role visitorRole = roleRepository.findByName(Role.RoleName.ROLE_VISITOR)
			.orElseThrow(() -> new RuntimeException("游客角色不存在"));

		// 创建游客信息Map
		Map<String, Object> guestInfo = new HashMap<>();
		guestInfo.put("username", username);
		guestInfo.put("password", passwordEncoder.encode(password));
		guestInfo.put("role", visitorRole.getName().name());

		// 存储到Redis并设置过期时间
		String redisKey = REDIS_KEY_PREFIX + username;
		redisTemplate.opsForHash().putAll(redisKey, guestInfo);
		redisTemplate.expire(redisKey, guestExpirationMs, TimeUnit.MILLISECONDS);

		log.info("游客信息已保存到Redis，过期时间: {}毫秒", guestExpirationMs);
	}

	@Override
	public Map<String, Object> getGuestFromRedis(String username) {
		String redisKey = REDIS_KEY_PREFIX + username;
		Map<String, Object> result = new HashMap<>();
		redisTemplate.opsForHash().entries(redisKey).forEach((key, value) -> {
			if (key instanceof String) {
				result.put((String) key, value);
			}
		});
		return result;
	}

	@Override
	public boolean existsInRedis(String username) {
		String redisKey = REDIS_KEY_PREFIX + username;
		return redisTemplate.hasKey(redisKey);
	}

	@Override
	public String generateGuestToken(String username) {
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("用户名不能为空");
		}

		log.info("为游客生成JWT令牌: {}", username);

		// 从Redis获取游客信息
		Map<String, Object> guestInfo = getGuestFromRedis(username);
		if (guestInfo.isEmpty()) {
			throw new RuntimeException("游客信息不存在于Redis");
		}

		// 创建UserDetails
		String role = (String) guestInfo.get("role");
		UserDetails userDetails = User.builder()
			.username(username)
			.password((String) guestInfo.get("password"))
			.authorities(new SimpleGrantedAuthority(role))
			.build();

		// 创建Authentication
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		// 生成具有自定义过期时间的JWT令牌
		return jwtTokenProvider.generateTokenWithExpiration(authentication, guestExpirationMs);
	}

}