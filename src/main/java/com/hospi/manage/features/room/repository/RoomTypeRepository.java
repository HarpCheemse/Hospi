package com.hospi.manage.features.room.repository;

import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByCategoryAndTierAndActiveTrue(RoomCategory category, RoomTier tier);

    boolean existsByCategoryAndTierAndIdNotAndActiveTrue(RoomCategory category, RoomTier tier, Long id);

    List<RoomType> findByActiveTrue();

    @Query("SELECT DISTINCT rt FROM RoomType rt LEFT JOIN FETCH rt.pictures WHERE rt.active = true")
    List<RoomType> findAllWithRelations();
}
