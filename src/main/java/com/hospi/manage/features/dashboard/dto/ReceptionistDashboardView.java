package com.hospi.manage.features.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** View model for the receptionist dashboard page. */
public record ReceptionistDashboardView(
        List<QueueItemView> todayQueue,
        int cleanedCount,
        int dirtyCount,
        int maintenanceCount,
        int inspectionCount,
        long arrivalsToday,
        long checkedInToday,
        long departuresToday,
        long waitingCheckoutToday,
        int availableRooms,
        int cleanedReadyRooms,
        BigDecimal pendingPaymentsAmount,
        int invoicesNeedFollowUp
) {}
