package com.website.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.website.backend.entity.Role;
import com.website.backend.repository.UserRepository;
import com.website.backend.repository.RoleRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisUserDetailsService implements UserDetailsService {

	private static final String GUEST_PREFIX = "guest_";

	private static final String REDIS_KEY_PREFIX = "guest:";

	/**
	 * 掩码用户名，保护用户隐私
	 * @param username 原始用户名
	 * @return 掩码后的用户名
	 */
	private String maskUsername(String username) {
		if (username == null) {
			return null;
		}

		if (username.startsWith(GUEST_PREFIX)) {
			// 游客用户保留前缀，掩码后面部分
			return GUEST_PREFIX + "***";
		}
		else if (username.length() <= 3) {
			// 短用户名全部掩码
			return "***";
		}
		else {
			// 普通用户显示前3个字符，其余掩码
			return username.substring(0, 3) + "***";
		}
	}

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("尝试加载用户: {}", maskUsername(username));

		// 检查是否是游客用户
		if (username.startsWith(GUEST_PREFIX)) {
			log.info("用户 {} 是游客，尝试从Redis加载", maskUsername(username));
			return loadGuestUserFromRedis(username);
		}
		else {
			log.info("用户 {} 是普通用户，尝试从数据库加载", maskUsername(username));
			return loadRegularUserFromDatabase(username);
		}
	}

	private UserDetails loadGuestUserFromRedis(String username) {
		String redisKey = REDIS_KEY_PREFIX + username;
		Map<Object, Object> guestInfo = redisTemplate.opsForHash().entries(redisKey);

		if (guestInfo.isEmpty()) {
			log.warn("游客 {} 在Redis中不存在", maskUsername(username));
			throw new UsernameNotFoundException("游客不存在: " + username);
		}

		String password = (String) guestInfo.get("password");

		// 获取游客角色
		Role visitorRole = roleRepository.findByName(Role.RoleName.ROLE_VISITOR)
			.orElseThrow(() -> new UsernameNotFoundException("游客角色不存在"));

		List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(visitorRole.getName().name()));

		log.info("成功从Redis加载游客 {}", maskUsername(username));
		return new User(username, password, authorities);
	}

	private UserDetails loadRegularUserFromDatabase(String username) {
		com.website.backend.entity.User user = userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

		List<SimpleGrantedAuthority> authorities = user.getRoles()
			.stream()
			.map(role -> new SimpleGrantedAuthority(role.getName().name()))
			.toList();

		log.info("成功从数据库加载用户 {}", maskUsername(username));
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				authorities);
	}

}