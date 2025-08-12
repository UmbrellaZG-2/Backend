package com.website.backend.constant;

/**
 * HTTP状态码常量类 用于统一管理项目中使用的HTTP响应状态码
 */
public class HttpStatusConstants {

	// 成功状态码
	public static final int SUCCESS = 200; // 成功

	public static final int CREATED = 201; // 创建成功

	public static final int NO_CONTENT = 204; // 无内容

	// 客户端错误状态码
	public static final int BAD_REQUEST = 400; // 请求错误

	public static final int UNAUTHORIZED = 401; // 未授权

	public static final int FORBIDDEN = 403; // 禁止访问

	public static final int NOT_FOUND = 404; // 资源未找到

	public static final int METHOD_NOT_ALLOWED = 405; // 方法不允许

	public static final int REQUEST_TIMEOUT = 408; // 请求超时

	public static final int CONFLICT = 409; // 冲突

	public static final int UNSUPPORTED_MEDIA_TYPE = 415; // 不支持的媒体类型

	// 服务器错误状态码
	public static final int INTERNAL_SERVER_ERROR = 500; // 服务器内部错误

	public static final int NOT_IMPLEMENTED = 501; // 未实现

	public static final int BAD_GATEWAY = 502; // 网关错误

	public static final int SERVICE_UNAVAILABLE = 503; // 服务不可用

	public static final int GATEWAY_TIMEOUT = 504; // 网关超时

	// 自定义业务状态码
	public static final int FILE_FORMAT_ERROR = 41501; // 文件格式错误

	public static final int FILE_SIZE_EXCEED = 41301; // 文件大小超出限制

	// 私有构造函数，防止实例化
	private HttpStatusConstants() {
		throw new AssertionError("不能实例化HttpStatusConstants类");
	}

}