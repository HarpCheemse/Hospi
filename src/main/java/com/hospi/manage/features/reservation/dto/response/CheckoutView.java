package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.Reservation;

public record CheckoutView(
        Reservation reservation,
        CheckoutCalculation calc,
        int adultGuests
) {}
