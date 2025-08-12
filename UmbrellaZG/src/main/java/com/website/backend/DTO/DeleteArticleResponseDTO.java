package com.website.backend.DTO;

import lombok.Data;

/**
 * 删除文章响应数据传输对象 用于封装删除文章操作的响应信息
 */
@Data
public class DeleteArticleResponseDTO {

	private boolean success;

	private String message;

}