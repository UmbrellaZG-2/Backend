package com.website.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.website.backend.entity.Comment;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByArticleId(Long articleId);

	List<Comment> findByParentId(Long parentId);

}