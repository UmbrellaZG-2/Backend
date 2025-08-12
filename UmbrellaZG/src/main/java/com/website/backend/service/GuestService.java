package com.website.backend.service;

import java.util.Map;

public interface GuestService {

	/**
	 * 生成游客用户名
	 */
	String generateGuestUsername();

	/**
	 * 保存游客信息到Redis
	 */
	void saveGuestToRedis(String username, String password);

	/**
	 * 从Redis获取游客信息
	 */
	Map<String, Object> getGuestFromRedis(String username);

	/**
	 * 检查游客是否存在于Redis
	 */
	boolean existsInRedis(String username);

	/**
	 * 生成游客JWT令牌
	 */
	String generateGuestToken(String username);

}