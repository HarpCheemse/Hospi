package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.RoomTypePicture;
import com.hospi.manage.features.room.enums.BedType;

import java.math.BigDecimal;
import java.util.List;

/** View model for room type availability with pictures and counts. */
public record RoomTypeAvailabilityView(
        Long roomTypeId,
        String name,
        Integer maxOccupancy,
        BigDecimal basePrice,
        BedType bedType,
        Integer area,
        String features,
        Boolean active,
        List<RoomTypePicture> pictures,
        int totalRooms,
        int availableRooms
) {
    public RoomTypeAvailabilityView(
            RoomTypeAvailability availability
    ) {
        this(
                availability.roomType().getId(),
                availability.roomType().getName(),
                availability.roomType().getMaxOccupancy().intValue(),
                availability.roomType().getBasePrice(),
                availability.roomType().getBedType(),
                availability.roomType().getArea(),
                availability.roomType().getFeatures(),
                availability.roomType().getActive(),
                availability.roomType().getPictures(),
                availability.totalRooms(),
                availability.availableRooms()
        );
    }

}
