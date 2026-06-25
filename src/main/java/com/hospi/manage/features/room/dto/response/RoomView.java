package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;

/** View model for a single room in a list. */
public record RoomView(
        Long id,
        String roomNumber,
        String roomTypeName,
        OccupancyStatus occupancyStatus,
        ConditionStatus conditionStatus
) {
}
