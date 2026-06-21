package com.hospi.manage.features.receptionist.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record CheckoutForm(
        Boolean applyLateFee,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actualCheckoutTime,
        String paymentMethod
) {}
