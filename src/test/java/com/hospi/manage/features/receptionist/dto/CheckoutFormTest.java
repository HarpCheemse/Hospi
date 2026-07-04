package com.hospi.manage.features.receptionist.dto;

import com.hospi.manage.features.reservation.dto.response.CheckoutForm;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutFormTest {

    @Test
    void shouldCreateWithApplyLateFee() {
        CheckoutForm form = new CheckoutForm(true, null, null);
        assertTrue(form.applyLateFee());
    }

    @Test
    void shouldCreateWithActualCheckoutTime() {
        LocalDateTime time = LocalDateTime.of(2026, 6, 26, 11, 0);
        CheckoutForm form = new CheckoutForm(null, time, null);
        assertEquals(time, form.actualCheckoutTime());
    }

    @Test
    void shouldCreateWithPaymentMethod() {
        CheckoutForm form = new CheckoutForm(null, null, "CASH");
        assertEquals("CASH", form.paymentMethod());
    }

    @Test
    void shouldDefaultToNulls() {
        CheckoutForm form = new CheckoutForm(null, null, null);
        assertNull(form.applyLateFee());
        assertNull(form.actualCheckoutTime());
        assertNull(form.paymentMethod());
    }
}
