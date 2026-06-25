package com.hospi.manage.features.manager.detail.repository;

import com.hospi.manage.features.manager.detail.entity.HotelPicture;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link HotelPicture} entity — provides access to hotel image data.
 */
public interface HotelPictureRepository extends JpaRepository<HotelPicture, Long> {
}
