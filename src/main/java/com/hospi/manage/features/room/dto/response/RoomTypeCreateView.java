package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;

/** View model for the room type creation page dropdown options. */
public record RoomTypeCreateView(
        RoomCategory[] categories,
        RoomTier[] tiers,
        BedType[] bedTypes
) {

    /** Create a default view with all enum values for dropdowns. */
    public static RoomTypeCreateView defaultView() {
        return new RoomTypeCreateView(
                RoomCategory.values(),
                RoomTier.values(),
                BedType.values()
        );
    }
}
