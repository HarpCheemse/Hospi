package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.RoomAssignment;

/**
 * View model for a room assignment record.
 * No JPA entities are exposed.
 */
public record RoomAssignmentView(
        Long id,
        String roomNumber,
        Long roomTypeId,
        String roomTypeName,
        Short floorNumber
) {
    /** Create from a JPA entity. */
    public static RoomAssignmentView from(RoomAssignment assignment) {
        return new RoomAssignmentView(
                assignment.getId(),
                assignment.getRoom().getRoomNumber(),
                assignment.getRoom().getRoomType().getId(),
                assignment.getRoom().getRoomType().getName(),
                assignment.getRoom().getFloorNumber()
        );
    }
}
