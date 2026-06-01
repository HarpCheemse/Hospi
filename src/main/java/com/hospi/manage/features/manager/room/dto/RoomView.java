package com.hospi.manage.features.manager.room.dto;

import com.hospi.manage.features.manager.room.enums.ConditionStatus;
import com.hospi.manage.features.manager.room.enums.OccupancyStatus;

public record RoomView(
        Long id,
        String roomNumber,
        String roomTypeName,
        OccupancyStatus occupancyStatus,
        ConditionStatus conditionStatus
) {}