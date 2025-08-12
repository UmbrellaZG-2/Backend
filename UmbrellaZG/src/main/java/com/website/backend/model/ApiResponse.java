package com.website.backend.model;

import com.website.backend.constant.HttpStatusConstants;
import lombok.Data;

/**
 * 统一API响应结果类 用于封装所有API的返回结果，包含状态码、消息和数据
 */
@Data
public class ApiResponse<T> {

	private int code;

	private String message;

	private T data;

	// 添加公共无参构造函数，用于Jackson反序列化
	public ApiResponse() {
	}

	// 私有构造函数
	private ApiResponse(int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	// 成功响应 - 带数据
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(HttpStatusConstants.SUCCESS, "操作成功", data);
	}

	// 成功响应 - 不带数据
	public static ApiResponse<Void> success() {
		return new ApiResponse<>(HttpStatusConstants.SUCCESS, "操作成功", null);
	}

	// 成功响应 - 自定义消息
	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(HttpStatusConstants.SUCCESS, message, data);
	}

	// 失败响应 - 自定义状态码和消息
	public static <T> ApiResponse<T> fail(int code, String message) {
		return new ApiResponse<>(code, message, null);
	}

	// 失败响应 - 使用预定义状态码
	public static <T> ApiResponse<T> fail(int code, String message, T data) {
		return new ApiResponse<>(code, message, data);
	}

	// 404错误
	public static ApiResponse<Void> notFound() {
		return new ApiResponse<>(HttpStatusConstants.NOT_FOUND, "资源未找到", null);
	}

	// 401错误
	public static ApiResponse<Void> unauthorized() {
		return new ApiResponse<>(HttpStatusConstants.UNAUTHORIZED, "未授权", null);
	}

	// 403错误
	public static ApiResponse<Void> forbidden() {
		return new ApiResponse<>(HttpStatusConstants.FORBIDDEN, "禁止访问", null);
	}

	// 500错误
	public static ApiResponse<Void> serverError() {
		return new ApiResponse<>(HttpStatusConstants.INTERNAL_SERVER_ERROR, "服务器内部错误", null);
	}

	// 文件格式错误
	public static ApiResponse<Void> fileFormatError() {
		return new ApiResponse<>(HttpStatusConstants.FILE_FORMAT_ERROR, "文件格式错误", null);
	}

	@Override
	public String toString() {
		return "ApiResponse{" + "code=" + code + ", message='" + message + '\'' + ", data=" + data + '}';
	}

}