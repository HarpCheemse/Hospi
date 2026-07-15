package com.hospi.manage.features.receptionist.dto;

import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.reservation.dto.request.CheckoutForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutFormTest {

    @Test
    void shouldCreateWithPaymentMethodAndLateFee() {
        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, true);
        assertEquals(PaymentMethod.CASH, form.paymentMethod());
        assertTrue(form.applyLateCheckoutFee());
    }

    @Test
    void shouldCreateWithCardNoLateFee() {
        CheckoutForm form = new CheckoutForm(PaymentMethod.CARD, false);
        assertEquals(PaymentMethod.CARD, form.paymentMethod());
        assertFalse(form.applyLateCheckoutFee());
    }
}
