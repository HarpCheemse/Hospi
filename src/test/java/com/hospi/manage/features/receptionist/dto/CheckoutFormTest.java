package com.hospi.manage.features.receptionist.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutFormTest {

    @Test
    void shouldSetAndGetApplyLateFee() {
        CheckoutForm form = new CheckoutForm();
        form.setApplyLateFee(true);
        assertTrue(form.isApplyLateFee());
    }

    @Test
    void shouldSetAndGetActualCheckoutTime() {
        CheckoutForm form = new CheckoutForm();
        LocalDateTime time = LocalDateTime.of(2026, 6, 26, 11, 0);
        form.setActualCheckoutTime(time);
        assertEquals(time, form.getActualCheckoutTime());
    }

    @Test
    void shouldSetAndGetExtraGuestFeeOverride() {
        CheckoutForm form = new CheckoutForm();
        form.setExtraGuestFeeOverride(BigDecimal.valueOf(25));
        assertEquals(BigDecimal.valueOf(25), form.getExtraGuestFeeOverride());
    }

    @Test
    void shouldSetAndGetAmountReceived() {
        CheckoutForm form = new CheckoutForm();
        form.setAmountReceived(BigDecimal.valueOf(1000));
        assertEquals(BigDecimal.valueOf(1000), form.getAmountReceived());
    }

    @Test
    void shouldSetAndGetPaymentMethod() {
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod("CASH");
        assertEquals("CASH", form.getPaymentMethod());
    }
}
