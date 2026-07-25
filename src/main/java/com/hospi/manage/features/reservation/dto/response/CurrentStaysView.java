package com.hospi.manage.features.reservation.dto.response;

import java.util.List;

/** View model for the current stays list page with pagination. */
public record CurrentStaysView(
    List<ReservationListItemView> guests,
    String checkedInSearch,
    int page,
    int totalPages,
    long total,
    String checkoutFilter
) {}