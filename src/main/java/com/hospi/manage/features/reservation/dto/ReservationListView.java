package com.hospi.manage.features.reservation.dto;

import java.time.LocalDate;
import java.util.List;

public record ReservationListView(
    List<ReservationListItemView> checkedIn,
    List<ReservationListItemView> active,
    String filterStatus,
    LocalDate filterDate,
    String filterSearch,
    String checkedInSearch,
    int checkedInPage,
    int checkedInTotalPages,
    long checkedInTotal,
    int activePage,
    int activeTotalPages,
    long activeTotal
) {}
