package com.hospi.manage.features.reservation.dto.response;

import java.util.List;

/** View model for the reservation creation details page. */
public record CreateDetailsView(
    List<RoomTypeAvailabilityView> roomTypes,
    long nights
) {}
