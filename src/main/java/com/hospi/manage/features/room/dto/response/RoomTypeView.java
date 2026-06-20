package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;

import java.math.BigDecimal;
import java.util.List;

public record RoomTypeView(Long id, String name, RoomCategory category, RoomTier tier, BedType bedType, Integer area,
                           Integer maxOccupancy, BigDecimal basePrice, int roomCount, Long coverPictureId,
                           String description, String features, List<Long> additionalPictureId,
                           RoomCategory[] categories,
                           RoomTier[] tiers,
                           BedType[] bedTypes) {

    public static RoomTypeView from(RoomType roomType) {

        return new RoomTypeView(roomType.getId(),
                roomType.getName(),
                roomType.getCategory(),
                roomType.getTier(),
                roomType.getBedType(),
                roomType.getArea(),
                roomType.getMaxOccupancy(),
                roomType.getBasePrice(),
                roomType.getRooms().size(),
                roomType.getCoverPictureId(),
                roomType.getDescription(),
                roomType.getFeatures(),
                roomType.getPicturesExcludeCover(),
                RoomCategory.values(),
                RoomTier.values(),
                BedType.values());
    }
}
