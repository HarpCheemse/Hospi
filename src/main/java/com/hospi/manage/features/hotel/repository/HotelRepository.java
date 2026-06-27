package com.hospi.manage.features.hotel.repository;

import com.hospi.manage.features.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Hotel} entity — provides access to hotel profile data.
 */
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
