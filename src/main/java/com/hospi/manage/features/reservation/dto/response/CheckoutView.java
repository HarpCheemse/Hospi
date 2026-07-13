package com.hospi.manage.features.reservation.dto.response;

/**
 * View model for the checkout page.
 * No JPA entities are exposed.
 */
public record CheckoutView(
        ReservationSummaryView reservation,
        CheckoutCalculation calc,
        int adultGuests
) {}
