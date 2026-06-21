package com.hospi.manage.features.receptionist.dto;

import com.hospi.manage.features.reservation.entity.Reservation;

public record CheckoutView(
        Reservation reservation,
        CheckoutCalculation calc,
        int adultGuests
) {}
