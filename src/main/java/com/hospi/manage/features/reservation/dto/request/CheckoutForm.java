package com.hospi.manage.features.reservation.dto.request;

import com.hospi.manage.features.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/** Form backing the checkout POST — payment method and late fee toggle. */
public record CheckoutForm(
        @NotNull PaymentMethod paymentMethod,
        boolean applyLateCheckoutFee
) {}
