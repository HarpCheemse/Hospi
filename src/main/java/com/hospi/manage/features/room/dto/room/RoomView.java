package com.hospi.manage.features.room.dto.room;

import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;

public record RoomView(
        Long id,
        String roomNumber,
        String roomTypeName,
        OccupancyStatus occupancyStatus,
        ConditionStatus conditionStatus
) {
}