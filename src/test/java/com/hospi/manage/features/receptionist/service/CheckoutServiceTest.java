package com.hospi.manage.features.receptionist.service;

import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.response.CheckoutCalculation;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.service.CheckoutService;
import com.hospi.manage.features.room.entity.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    PaymentRepository paymentRepository;
    @Mock
    SystemConfigService systemConfigService;
    @Mock
    InvoiceRepository invoiceRepository;

    CheckoutService checkoutService;

    SystemConfig config;
    RoomType roomType;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(reservationRepository, paymentRepository, systemConfigService, invoiceRepository);

        config = new SystemConfig();
        config.setDefaultDepositPercentage(BigDecimal.valueOf(20));
        config.setLateCheckoutFee(BigDecimal.valueOf(50));
        config.setExtraGuestFee(BigDecimal.valueOf(25));

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe");
        roomType.setMaxOccupancy(2);
    }

    @Test
    void calculate_offlineBooking_noFees() {
        when(systemConfigService.getConfig()).thenReturn(config);

        Reservation r = new Reservation();
        r.setId(1L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.of(2026, 6, 15));
        r.setCheckOutAt(LocalDate.of(2026, 6, 18));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(2);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(400));
        r.setDetails(List.of(detail));

        when(paymentRepository.findAllByReservationId(1L)).thenReturn(List.of());

        CheckoutCalculation calc = checkoutService.calculate(r, null, 2);

        assertEquals(BigDecimal.valueOf(1000), calc.totalPrice());
        assertEquals(BigDecimal.ZERO, calc.depositPaid());
        assertEquals(BigDecimal.valueOf(1000), calc.remainingBalance());
        assertEquals(BigDecimal.ZERO, calc.lateCheckoutFee());
        assertEquals(BigDecimal.ZERO, calc.extraGuestFee());
        assertEquals(BigDecimal.valueOf(1000), calc.totalDue());
        assertFalse(calc.isLate());
        assertEquals(0, calc.extraGuestCount());
    }

    @Test
    void calculate_offlineBooking_lateFeeAndExtraGuest() {
        when(systemConfigService.getConfig()).thenReturn(config);

        Reservation r = new Reservation();
        r.setId(2L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.of(2026, 6, 15));
        r.setCheckOutAt(LocalDate.of(2026, 6, 18));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(200));
        r.setDetails(List.of(detail));

        when(paymentRepository.findAllByReservationId(2L)).thenReturn(List.of());

        LocalDateTime lateTime = LocalDateTime.of(LocalDate.of(2026, 6, 18), LocalTime.of(14, 0));

        CheckoutCalculation calc = checkoutService.calculate(r, lateTime, 4);

        assertEquals(BigDecimal.ZERO, calc.depositPaid());
        assertTrue(calc.isLate());
        assertEquals(BigDecimal.valueOf(50), calc.lateCheckoutFee());
        assertEquals(2, calc.extraGuestCount());
        assertEquals(BigDecimal.valueOf(50), calc.extraGuestFee());
        assertEquals(BigDecimal.valueOf(1100), calc.totalDue());
    }

    @Test
    void calculate_onlineBooking_withDeposit() {
        when(systemConfigService.getConfig()).thenReturn(config);

        Reservation r = new Reservation();
        r.setId(3L);
        r.setSource(BookingSource.ONLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.of(2026, 6, 15));
        r.setCheckOutAt(LocalDate.of(2026, 6, 18));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(200));
        r.setDetails(List.of(detail));

        Payment depositPayment = new Payment();
        depositPayment.setAmount(BigDecimal.valueOf(200));
        when(paymentRepository.findAllByReservationId(3L)).thenReturn(List.of(depositPayment));

        CheckoutCalculation calc = checkoutService.calculate(r, null, 2);

        assertEquals(BigDecimal.valueOf(200), calc.depositPaid());
        assertEquals(BigDecimal.valueOf(800), calc.remainingBalance());
        assertEquals(0, calc.extraGuestCount());
        assertEquals(BigDecimal.valueOf(800), calc.totalDue());
    }

    @Test
    void complete_checkedIn_checkoutSuccess() {
        when(systemConfigService.getConfig()).thenReturn(config);

        Reservation r = new Reservation();
        r.setId(1L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setStatus(ReservationStatus.CHECKED_IN);
        r.setCheckInAt(LocalDate.of(2026, 6, 15));
        r.setCheckOutAt(LocalDate.of(2026, 6, 18));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenReturn(r);

        CheckoutCalculation calc = new CheckoutCalculation(
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                false,
                0
        );

        Reservation result = checkoutService.complete(1L, calc,
                PaymentMethod.CASH, "receptionist", null, false);

        assertEquals(ReservationStatus.CHECKED_OUT, result.getStatus());
        assertNotNull(result.getCheckedOutAt());
        assertEquals("receptionist", result.getCheckedOutBy());
        assertEquals(BigDecimal.ZERO, result.getLateCheckoutFeeApplied());
        assertEquals(BigDecimal.ZERO, result.getExtraGuestFeeApplied());
        verify(paymentRepository).save(any());
    }

    @Test
    void complete_createsInvoice() {
        when(systemConfigService.getConfig()).thenReturn(config);

        Reservation r = new Reservation();
        r.setId(1L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setStatus(ReservationStatus.CHECKED_IN);
        r.setCheckInAt(LocalDate.of(2026, 6, 15));
        r.setCheckOutAt(LocalDate.of(2026, 6, 18));

        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setName("Deluxe");
        rt.setMaxOccupancy(2);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(rt);
        detail.setRoomCount(2);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(400));
        r.setDetails(List.of(detail));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenReturn(r);
        when(paymentRepository.findAllByReservationId(1L)).thenReturn(List.of());

        CheckoutCalculation calc = new CheckoutCalculation(
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                false,
                0
        );

        checkoutService.complete(1L, calc, PaymentMethod.CASH, "receptionist", null, false);

        verify(invoiceRepository).save(any());
    }

    @Test
    void complete_autoCheckIn_whenConfirmed() {
        when(systemConfigService.getConfig()).thenReturn(config);

        Reservation r = new Reservation();
        r.setId(3L);
        r.setSource(BookingSource.ONLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setStatus(ReservationStatus.CONFIRMED);
        r.setCheckInAt(LocalDate.of(2026, 6, 15));
        r.setCheckOutAt(LocalDate.of(2026, 6, 18));

        when(reservationRepository.findById(3L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenReturn(r);

        CheckoutCalculation calc = new CheckoutCalculation(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(800),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(800),
                false,
                0
        );

        Reservation result = checkoutService.complete(3L, calc,
                PaymentMethod.CARD, "receptionist", null, false);

        assertEquals(ReservationStatus.CHECKED_OUT, result.getStatus());
        assertNotNull(result.getCheckedInAt());
        assertEquals("receptionist", result.getCheckedInBy());
    }
}
