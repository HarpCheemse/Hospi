package com.hospi.manage.features.reservation.dto.response;

/** View model for a room type name and count summary. */
public record RoomDetailView(
        String roomTypeName,
        Integer roomCount
) {
}
