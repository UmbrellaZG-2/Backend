package com.website.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.website.backend.entity.SystemConfig;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

	Optional<SystemConfig> findByConfigKey(String configKey);

}