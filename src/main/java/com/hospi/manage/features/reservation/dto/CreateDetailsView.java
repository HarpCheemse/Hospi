package com.hospi.manage.features.reservation.dto;

import java.util.List;

public record CreateDetailsView(
    List<RoomTypeAvailabilityView> roomTypes,
    long nights
) {}
