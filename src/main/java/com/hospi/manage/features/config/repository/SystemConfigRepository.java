package com.hospi.manage.features.config.repository;

import com.hospi.manage.features.config.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link SystemConfig} entity — provides access to hotel-level configuration.
 */
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
}
