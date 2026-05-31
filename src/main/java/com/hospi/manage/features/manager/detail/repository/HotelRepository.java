package com.hospi.manage.features.manager.detail.repository;

import com.hospi.manage.features.manager.detail.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
