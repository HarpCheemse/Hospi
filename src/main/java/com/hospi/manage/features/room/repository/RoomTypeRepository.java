package com.hospi.manage.features.room.repository;

import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByCategoryAndTier(RoomCategory category, RoomTier tier);

    boolean existsByCategoryAndTierAndIdNot(RoomCategory category, RoomTier tier, Long id);
}
