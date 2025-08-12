package com.website.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.website.backend.entity.Role;
import java.util.Optional;

@Repository

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByName(Role.RoleName name);
	// 继承自JpaRepository的方法已包含findById等CRUD操作

}