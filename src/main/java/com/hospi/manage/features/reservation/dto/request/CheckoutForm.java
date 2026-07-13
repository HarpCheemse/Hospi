package com.hospi.manage.features.reservation.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** Form backing the checkout flow — payment method, late fee toggle, and actual checkout time. */
public record CheckoutForm(
        Boolean applyLateFee,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actualCheckoutTime,
        String paymentMethod
) {}
