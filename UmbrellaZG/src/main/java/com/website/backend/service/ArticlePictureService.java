package com.website.backend.service;

import com.website.backend.entity.Article;
import com.website.backend.entity.ArticlePicture;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ArticlePictureService {

	ArticlePicture uploadPicture(MultipartFile file, Article article) throws IOException;

	byte[] downloadPicture(Long pictureId) throws IOException;

	void deletePicture(Long pictureId);

	void deletePictureByArticle(Article article);

	ArticlePicture getPictureByArticle(Article article);

}