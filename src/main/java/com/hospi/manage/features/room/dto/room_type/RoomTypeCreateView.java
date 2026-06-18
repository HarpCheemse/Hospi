package com.hospi.manage.features.room.dto.room_type;

import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;

public record RoomTypeCreateView(
        RoomCategory[] categories,
        RoomTier[] tiers,
        BedType[] bedTypes
) {

    public static RoomTypeCreateView defaultView() {
        return new RoomTypeCreateView(
                RoomCategory.values(),
                RoomTier.values(),
                BedType.values()
        );
    }
}