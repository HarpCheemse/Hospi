package com.hospi.manage.features.admin.config.repository;

import com.hospi.manage.features.admin.config.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
}
