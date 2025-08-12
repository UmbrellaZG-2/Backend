package com.website.backend.service;

public interface RateLimitService {

	/**
	 * 检查IP是否被禁止下载
	 * @param ip 用户IP地址
	 * @return true如果IP被禁止，false如果可以继续下载
	 */
	boolean isIpBlocked(String ip);

	/**
	 * 记录IP的下载请求
	 * @param ip 用户IP地址
	 * @return true如果超过限制，false如果未超过限制
	 */
	boolean recordDownloadRequest(String ip);

	/**
	 * 将IP添加到黑名单
	 * @param ip 用户IP地址
	 */
	void blockIp(String ip);

}