package com.website.backend.service.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.website.backend.entity.Role;
import com.website.backend.entity.User;
import com.website.backend.repository.RoleRepository;
import com.website.backend.repository.UserRepository;
import com.website.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

	private final PasswordEncoder passwordEncoder;

	/**
	 * 构造函数注入依赖
	 * @param userRepository 用户仓库
	 * @param roleRepository 角色仓库
	 * @param passwordEncoder 密码编码器
	 */
	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public User registerUser(String username, String password) {
		log.info("开始注册用户: {}", username);
		// 检查用户名是否已存在
		if (userRepository.existsByUsername(username)) {
			log.error("用户名已存在: {}", username);
			throw new RuntimeException("用户名已存在: " + username);
		}

		// 创建新用户
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		log.info("用户信息创建完成: {}", username);

		// 设置用户角色为访客
		Set<Role> roles = new HashSet<>();
		Role visitorRole = roleRepository.findByName(Role.RoleName.ROLE_VISITOR).orElseThrow(() -> {
			log.error("角色不存在: ROLE_VISITOR");
			return new RuntimeException("角色不存在: ROLE_VISITOR");
		});
		roles.add(visitorRole);
		user.setRoles(roles);
		log.info("用户角色设置完成: {}, 角色: {}", username, roles);

		User savedUser = userRepository.save(user);
		log.info("用户注册成功: {}", savedUser.getUsername());
		return savedUser;
	}

	@Override
	public Optional<User> findByUsername(String username) {
		log.info("查询用户: {}", username);
		return userRepository.findByUsername(username);
	}

	@Override
	public boolean existsByUsername(String username) {
		boolean exists = userRepository.existsByUsername(username);
		log.info("检查用户是否存在: {}, 结果: {}", username, exists);
		return exists;
	}

}