package com.website.backend.exception;

import com.website.backend.constant.HttpStatusConstants;
import com.website.backend.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// 处理所有未捕获的异常
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<?> handleException(Exception e) {
		logger.error("发生未预期的异常: {}", e.getMessage(), e);
		return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "服务器内部错误: " + e.getMessage());
	}

	// 处理资源不存在异常
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiResponse<?> handleResourceNotFound(ResourceNotFoundException e) {
		logger.error("资源不存在: {}", e.getMessage());
		return ApiResponse.fail(HttpStatusConstants.NOT_FOUND, e.getMessage());
	}

	// 处理权限不足异常
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiResponse<?> handleAccessDenied(AccessDeniedException e) {
		logger.error("权限不足: {}", e.getMessage());
		return ApiResponse.fail(HttpStatusConstants.FORBIDDEN, "没有足够的权限执行此操作");
	}

	// 处理认证异常
	@ExceptionHandler(CustomAuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiResponse<?> handleAuthenticationException(CustomAuthenticationException e) {
		logger.error("认证失败: {}", e.getMessage());
		return ApiResponse.fail(HttpStatusConstants.UNAUTHORIZED, "认证失败: 用户名或密码错误");
	}

	// 处理文件上传异常
	@ExceptionHandler(FileUploadException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<?> handleFileUploadException(FileUploadException e) {
		logger.error("文件上传失败: {}", e.getMessage());
		return ApiResponse.fail(HttpStatusConstants.BAD_REQUEST, "文件上传失败: " + e.getMessage());
	}

	// 处理IO异常
	@ExceptionHandler(IOException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<?> handleIOException(IOException e) {
		logger.error("IO异常: {}", e.getMessage(), e);
		return ApiResponse.fail(HttpStatusConstants.INTERNAL_SERVER_ERROR, "文件操作失败: " + e.getMessage());
	}

}

// 自定义异常类已移至单独文件