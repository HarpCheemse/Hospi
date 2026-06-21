package com.hospi.manage.features.reservation.dto;

import java.util.List;

public record CurrentStaysView(
    List<ReservationListItemView> guests,
    String checkedInSearch,
    int page,
    int totalPages,
    long total
) {}