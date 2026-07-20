package com.hospi.manage.features.payment.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private PayPalService payPalService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<Reservation> reservationCaptor;

    // --- createOnlineBookingPayment ---

    @Test
    void shouldCreateOnlineBookingPayment_whenValidAmount() throws IOException {
        when(payPalService.createOrder("10.00", "http://return", "http://cancel"))
                .thenReturn("https://paypal.com/approval/url");

        String result = paymentService.createOnlineBookingPayment(
                new BigDecimal("10.00"), "http://return", "http://cancel");

        assertEquals("https://paypal.com/approval/url", result);
        verify(payPalService).createOrder("10.00", "http://return", "http://cancel");
    }

    @Test
    void shouldCreateOnlineBookingPayment_whenAmountWithManyDecimals() throws IOException {
        when(payPalService.createOrder("10.13", "http://return", "http://cancel"))
                .thenReturn("https://paypal.com/approval/url");

        String result = paymentService.createOnlineBookingPayment(
                new BigDecimal("10.13456"), "http://return", "http://cancel");

        assertEquals("https://paypal.com/approval/url", result);
        verify(payPalService).createOrder("10.13", "http://return", "http://cancel");
    }

    // --- captureOnlineBookingPayment ---

    @Test
    void shouldCaptureOnlineBookingPayment_whenValidOrder() throws IOException {
        when(payPalService.captureOrder("ORDER_123")).thenReturn(true);

        boolean result = paymentService.captureOnlineBookingPayment("ORDER_123");

        assertTrue(result);
        verify(payPalService).captureOrder("ORDER_123");
    }

    @Test
    void shouldReturnFalse_whenCaptureFails() throws IOException {
        when(payPalService.captureOrder("ORDER_123")).thenReturn(false);

        boolean result = paymentService.captureOnlineBookingPayment("ORDER_123");

        assertFalse(result);
        verify(payPalService).captureOrder("ORDER_123");
    }

    // --- confirmPayment ---

    @Test
    void shouldConfirmPayment_whenValidOfflineReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSource(BookingSource.OFFLINE);
        reservation.setTotalPrice(BigDecimal.valueOf(500));
        reservation.setGuestName("John Doe");

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.existsByReservationId(1L)).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.confirmPayment(1L, PaymentMethod.CASH, "Receptionist1");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        assertEquals(PaymentMethod.CASH, result.getPaymentMethod());
        assertEquals("Receptionist1", result.getConfirmedBy());
        assertNotNull(result.getConfirmedAt());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        verify(paymentRepository).save(paymentCaptor.capture());
        verify(reservationRepository).save(reservationCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(reservation, savedPayment.getReservation());
        assertEquals(BigDecimal.valueOf(500), savedPayment.getAmount());
        assertEquals(PaymentMethod.CASH, savedPayment.getPaymentMethod());
        assertEquals("Receptionist1", savedPayment.getConfirmedBy());

        Reservation savedReservation = reservationCaptor.getValue();
        assertEquals(ReservationStatus.CONFIRMED, savedReservation.getStatus());

        verify(notificationService).notifyRole(
                Role.RECEPTIONIST, "Payment Received",
                "Payment confirmed for John Doe (CASH)");
        verify(notificationService).notifyRole(
                Role.MANAGER, "Payment Received",
                "Payment confirmed for John Doe (CASH)");
    }

    @Test
    void shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.confirmPayment(99L, PaymentMethod.CASH, "Receptionist1"));
    }

    @Test
    void shouldThrow_whenReservationNotPending() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> paymentService.confirmPayment(1L, PaymentMethod.CARD, "Receptionist1"));
        assertTrue(ex.getMessage().contains("not in PENDING status"));
    }

    @Test
    void shouldThrow_whenReservationNotOffline() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSource(BookingSource.ONLINE);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> paymentService.confirmPayment(1L, PaymentMethod.CARD, "Receptionist1"));
        assertTrue(ex.getMessage().contains("Only offline bookings"));
    }

    @Test
    void shouldThrow_whenPaymentAlreadyExists() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSource(BookingSource.OFFLINE);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.existsByReservationId(1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> paymentService.confirmPayment(1L, PaymentMethod.CARD, "Receptionist1"));
        assertTrue(ex.getMessage().contains("Payment already exists"));
    }

    @Test
    void shouldNotSavePaymentOrReservation_whenConfirmPaymentFails() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.confirmPayment(1L, PaymentMethod.CASH, "Receptionist1"));

        verify(paymentRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(notificationService, never()).notifyRole(any(), any(), any());
    }

    // --- refundOnlineBookingPayment ---

    @Test
    void shouldRefundOnlineBookingPayment_whenValidOrder() throws IOException {
        when(payPalService.refundOrder("ORDER_123")).thenReturn(true);

        boolean result = paymentService.refundOnlineBookingPayment("ORDER_123");

        assertTrue(result);
        verify(payPalService).refundOrder("ORDER_123");
    }

    @Test
    void shouldReturnFalse_whenRefundFails() throws IOException {
        when(payPalService.refundOrder("ORDER_123")).thenReturn(false);

        boolean result = paymentService.refundOnlineBookingPayment("ORDER_123");

        assertFalse(result);
        verify(payPalService).refundOrder("ORDER_123");
    }

    // --- calculateRefund ---

    @Test
    void shouldReturnFullRefund_whenWithinFullRefundWindow() {
        SystemConfig config = new SystemConfig();
        config.setFullRefundWindowHours(24);
        config.setRefundPercentage(new BigDecimal("50"));

        Reservation reservation = new Reservation();
        reservation.setTotalPrice(BigDecimal.valueOf(200));
        reservation.setCreatedAt(LocalDateTime.now().minusHours(10));

        when(systemConfigService.getConfig()).thenReturn(config);

        BigDecimal refund = paymentService.calculateRefund(reservation);

        assertEquals(0, BigDecimal.valueOf(200).compareTo(refund));
    }

    @Test
    void shouldReturnPartialRefund_whenBeyondFullRefundWindow() {
        SystemConfig config = new SystemConfig();
        config.setFullRefundWindowHours(24);
        config.setRefundPercentage(new BigDecimal("50"));

        Reservation reservation = new Reservation();
        reservation.setTotalPrice(BigDecimal.valueOf(200));
        reservation.setCreatedAt(LocalDateTime.now().minusHours(48));

        when(systemConfigService.getConfig()).thenReturn(config);

        BigDecimal refund = paymentService.calculateRefund(reservation);

        assertEquals(0, BigDecimal.valueOf(100).compareTo(refund));
    }

    @Test
    void shouldReturnZeroRefund_whenPercentageIsZero() {
        SystemConfig config = new SystemConfig();
        config.setFullRefundWindowHours(24);
        config.setRefundPercentage(BigDecimal.ZERO);

        Reservation reservation = new Reservation();
        reservation.setTotalPrice(BigDecimal.valueOf(200));
        reservation.setCreatedAt(LocalDateTime.now().minusHours(48));

        when(systemConfigService.getConfig()).thenReturn(config);

        BigDecimal refund = paymentService.calculateRefund(reservation);

        assertEquals(0, BigDecimal.ZERO.compareTo(refund));
    }

    @Test
    void shouldReturnFullRefund_whenExactlyAtWindowEdge() {
        SystemConfig config = new SystemConfig();
        config.setFullRefundWindowHours(24);
        config.setRefundPercentage(new BigDecimal("50"));

        Reservation reservation = new Reservation();
        reservation.setTotalPrice(BigDecimal.valueOf(200));
        reservation.setCreatedAt(LocalDateTime.now().minusHours(24));

        when(systemConfigService.getConfig()).thenReturn(config);

        BigDecimal refund = paymentService.calculateRefund(reservation);

        assertEquals(0, BigDecimal.valueOf(200).compareTo(refund));
    }
}
