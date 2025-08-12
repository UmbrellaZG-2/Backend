package com.website.backend.service.impl;

import com.website.backend.service.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitServiceImpl implements RateLimitService {

	private final RedisTemplate<String, Object> redisTemplate;

	// 常量定义
	private static final String DOWNLOAD_RATE_LIMIT_PREFIX = "download:rate:limit:";

	private static final String BLACKLIST_PREFIX = "download:block:";

	private static final int TIME_WINDOW_SECONDS = 10; // 10秒时间窗口

	private static final int MAX_REQUESTS = 5; // 最大请求次数

	private static final long BLOCK_DURATION_DAYS = 1; // 黑名单持续时间（天）

	@Autowired
	public RateLimitServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public boolean isIpBlocked(String ip) {
		String key = BLACKLIST_PREFIX + ip;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	@Override
	public boolean recordDownloadRequest(String ip) {
		// 检查IP是否已被拉黑
		if (isIpBlocked(ip)) {
			return true;
		}

		String key = DOWNLOAD_RATE_LIMIT_PREFIX + ip;

		// 增加计数
		Long count = redisTemplate.opsForValue().increment(key);

		// 设置过期时间（仅在第一次计数时）
		if (count != null && count == 1) {
			redisTemplate.expire(key, TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
		}

		// 检查是否超过限制
		if (count != null && count > MAX_REQUESTS) {
			// 添加到黑名单
			blockIp(ip);
			return true;
		}

		return false;
	}

	@Override
	public void blockIp(String ip) {
		String key = BLACKLIST_PREFIX + ip;
		redisTemplate.opsForValue().set(key, true);
		redisTemplate.expire(key, BLOCK_DURATION_DAYS, TimeUnit.DAYS);
	}

}