package com.website.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.website.backend.entity.Visitor;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    // 继承自JpaRepository的方法已包含findById等CRUD操作
}
