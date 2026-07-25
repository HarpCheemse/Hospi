package com.hospi.manage.features.room.repository;

import com.hospi.manage.features.room.entity.RoomTypePicture;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link RoomTypePicture} entity — provides access to room type image data.
 */
public interface RoomTypePictureRepository extends JpaRepository<RoomTypePicture, Long> {

}
