package com.hospi.manage.features.room.repository;

import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for {@link RoomType} entity — provides room type lookup and uniqueness checks.
 */
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByCategoryAndTierAndActiveTrue(RoomCategory category, RoomTier tier);

    boolean existsByCategoryAndTierAndIdNotAndActiveTrue(RoomCategory category, RoomTier tier, Long id);

    List<RoomType> findByActiveTrue();

    /**
     * Find all active room types with their pictures eagerly loaded.
     */
    @Query("SELECT DISTINCT rt FROM RoomType rt LEFT JOIN FETCH rt.pictures WHERE rt.active = true")
    List<RoomType> findAllWithRelations();
}
