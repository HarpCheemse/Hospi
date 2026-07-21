package com.hospi.manage.features.dashboard.dto;

/** View model representing a single front-desk queue item. */
public record QueueItemView(
        Long reservationId,
        String guestName,
        String confirmationCode,
        String roomNumbers,
        String roomTypeNames,
        int guestCount,
        String time,
        String actionType,
        String paymentStatus
) {}
