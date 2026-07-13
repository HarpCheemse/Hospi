package com.hospi.manage.features.reservation.dto;

import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.reservation.dto.response.ReceiptView;
import com.hospi.manage.features.reservation.entity.Reservation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptViewTest {

    @Test
    void shouldMapCorrectly_withDepositAndCheckoutPayment() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setTotalPrice(BigDecimal.valueOf(1000));
        reservation.setLateCheckoutFeeApplied(BigDecimal.valueOf(50));
        reservation.setExtraGuestFeeApplied(BigDecimal.valueOf(25));
        reservation.setCheckedOutAt(LocalDateTime.of(2026, 6, 18, 11, 0));

        Payment deposit = new Payment();
        deposit.setAmount(BigDecimal.valueOf(200));
        deposit.setConfirmedAt(LocalDateTime.of(2026, 6, 15, 14, 0));

        Payment checkoutPay = new Payment();
        checkoutPay.setAmount(BigDecimal.valueOf(875));
        checkoutPay.setConfirmedAt(LocalDateTime.of(2026, 6, 18, 11, 1));

        ReceiptView view = ReceiptView.from(reservation, List.of(deposit, checkoutPay));

        assertEquals(BigDecimal.valueOf(1000), view.roomCharges());
        assertEquals(BigDecimal.valueOf(50), view.lateCheckoutFee());
        assertEquals(BigDecimal.valueOf(25), view.extraGuestFee());
        assertEquals(BigDecimal.valueOf(1075), view.totalCharges());
        assertEquals(BigDecimal.valueOf(200), view.depositPaid());
        assertEquals(BigDecimal.valueOf(875), view.checkoutPayment());
        assertEquals(BigDecimal.valueOf(1075), view.totalPaid());
    }
}
