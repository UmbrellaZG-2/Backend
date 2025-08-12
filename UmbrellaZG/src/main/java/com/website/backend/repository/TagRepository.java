package com.website.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.website.backend.entity.Tag;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

	Optional<Tag> findByName(String name);

}