package com.hospi.manage.features.reservation.dto.response;

import java.util.List;
import java.util.Map;

/**
 * View model for the reservation management page with guests, assignments, and available rooms.
 * No JPA entities are exposed — all fields are view records.
 */
public record ManageReservationView(
        ReservationSummaryView reservation,
        List<StayingGuestView> guests,
        List<RoomAssignmentView> assignedRooms,
        List<AvailableRoomView> availableRooms,
        Map<Long, Integer> assignedCounts
) {
    public int assignedCount(Long roomTypeId) {
        return assignedCounts.getOrDefault(roomTypeId, 0);
    }

    public long nights() {
        return reservation.nights();
    }

    /** True if the room type has assigned rooms but no matching reservation detail. */
    public boolean isOrphanedAssignment(Long roomTypeId) {
        return reservation.details().stream()
                .noneMatch(d -> d.roomTypeId().equals(roomTypeId));
    }

    /** True if there are any assigned rooms whose room type no longer exists on the reservation. */
    public boolean hasOrphanedAssignments() {
        return assignedRooms.stream()
                .anyMatch(a -> reservation.details().stream()
                        .noneMatch(d -> d.roomTypeId().equals(a.roomTypeId())));
    }
}
