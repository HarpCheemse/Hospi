package com.hospi.manage.features.receptionist.dto;

import com.hospi.manage.features.reservation.dto.request.CheckoutForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutFormTest {

    @Test
    void shouldCreateWithApplyLateFee() {
        CheckoutForm form = new CheckoutForm(true, null);
        assertTrue(form.applyLateFee());
    }

    @Test
    void shouldCreateWithPaymentMethod() {
        CheckoutForm form = new CheckoutForm(null, "CASH");
        assertEquals("CASH", form.paymentMethod());
    }

    @Test
    void shouldDefaultToNulls() {
        CheckoutForm form = new CheckoutForm(null, null);
        assertNull(form.applyLateFee());
        assertNull(form.paymentMethod());
    }
}
