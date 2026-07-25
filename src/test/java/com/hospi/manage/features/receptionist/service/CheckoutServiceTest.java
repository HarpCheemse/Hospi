package com.hospi.manage.features.receptionist.service;

import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.hotel.repository.HotelRepository;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.request.CheckoutForm;
import com.hospi.manage.features.reservation.dto.response.CheckoutView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.service.CheckoutService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.room.entity.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock SystemConfigService systemConfigService;
    @Mock InvoiceRepository invoiceRepository;
    @Mock RoomAssignmentService roomAssignmentService;
    @Mock HotelRepository hotelRepository;

    CheckoutService checkoutService;

    SystemConfig config;
    Hotel hotel;
    RoomType roomType;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(
                reservationRepository, paymentRepository, systemConfigService,
                invoiceRepository, roomAssignmentService, hotelRepository);

        config = new SystemConfig();
        config.setLateCheckoutFee(BigDecimal.valueOf(25));
        config.setTaxRate(BigDecimal.valueOf(8));

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setCheckOutTime(LocalTime.of(12, 0));

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe");
        roomType.setMaxOccupancy(2);
    }

    @Test
    void buildCheckoutView_shouldReturnView() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.of(2026, 6, 15));
        r.setCheckOutAt(LocalDate.now().plusDays(1));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(2);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(400));
        r.setDetails(List.of(detail));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(systemConfigService.getConfig()).thenReturn(config);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(paymentRepository.findAllByReservationId(1L)).thenReturn(List.of());
        when(roomAssignmentService.getAssignedRoomNumbers(1L)).thenReturn("101, 102");

        CheckoutView view = checkoutService.buildCheckoutView(1L);

        assertEquals(0, BigDecimal.valueOf(1000).compareTo(view.roomCharges()));
        assertEquals(0, BigDecimal.valueOf(80).compareTo(view.taxAmount()));
        assertEquals(0, BigDecimal.valueOf(1080).compareTo(view.totalCharges()));
        assertEquals(0, BigDecimal.ZERO.compareTo(view.depositPaid()));
        assertEquals(0, BigDecimal.valueOf(1080).compareTo(view.remainingDue()));
        assertEquals("101, 102", view.roomNumbers());
    }

    @Test
    void buildCheckoutView_shouldIncludeLateFee_whenPastCheckOut() {
        Reservation r = new Reservation();
        r.setId(2L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.now().minusDays(3));
        r.setCheckOutAt(LocalDate.now().minusDays(1));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(200));
        r.setDetails(List.of(detail));

        when(reservationRepository.findById(2L)).thenReturn(Optional.of(r));
        when(systemConfigService.getConfig()).thenReturn(config);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(paymentRepository.findAllByReservationId(2L)).thenReturn(List.of());
        when(roomAssignmentService.getAssignedRoomNumbers(2L)).thenReturn("201");

        CheckoutView view = checkoutService.buildCheckoutView(2L);

        assertTrue(view.showLateFee());
        assertTrue(view.lateCheckoutFee().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void complete_shouldCheckOut_whenCheckedIn() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.now().minusDays(2));
        r.setCheckOutAt(LocalDate.now().plusDays(1));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(200));
        r.setDetails(List.of(detail));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(systemConfigService.getConfig()).thenReturn(config);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(reservationRepository.save(any())).thenReturn(r);

        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, false);

        checkoutService.complete(1L, form, "receptionist");

        assertEquals(ReservationStatus.CHECKED_OUT, r.getStatus());
        assertNotNull(r.getCheckedOutAt());
        assertEquals("receptionist", r.getCheckedOutBy());
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getLateCheckoutFeeApplied()));
        verify(paymentRepository).save(any());
        verify(invoiceRepository).save(any());
        verify(roomAssignmentService).vacateAllReservationRooms(1L);
    }

    @Test
    void complete_shouldThrow_whenNotCheckedIn() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, false);

        assertThrows(IllegalStateException.class,
                () -> checkoutService.complete(1L, form, "receptionist"));
    }

    @Test
    void complete_shouldThrow_whenAlreadyCheckedOut() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.CHECKED_OUT);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, false);

        assertThrows(IllegalStateException.class,
                () -> checkoutService.complete(1L, form, "receptionist"));
    }

    @Test
    void buildCheckoutView_shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> checkoutService.buildCheckoutView(1L));
    }

    @Test
    void complete_shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, false);

        assertThrows(ResourceNotFoundException.class,
                () -> checkoutService.complete(1L, form, "user"));
    }

    @Test
    void complete_shouldThrow_whenInvoiceAlreadyExists() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.CHECKED_IN);

        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(1L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.of(existingInvoice));

        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> checkoutService.complete(1L, form, "user"));
        assertTrue(ex.getMessage().contains("Invoice already exists"));
    }

    @Test
    void complete_shouldApplyLateFee_whenReceptionistOptsInAndPastCheckOut() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.now().minusDays(3));
        r.setCheckOutAt(LocalDate.now().minusDays(1));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(200));
        r.setDetails(List.of(detail));

        Payment deposit = new Payment();
        deposit.setAmount(BigDecimal.valueOf(100));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(systemConfigService.getConfig()).thenReturn(config);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(paymentRepository.findAllByReservationId(1L)).thenReturn(List.of(deposit));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(roomAssignmentService.getAssignedRoomNumbers(1L)).thenReturn("101");
        when(reservationRepository.save(any())).thenReturn(r);

        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, true);
        checkoutService.complete(1L, form, "user");

        assertTrue(r.getLateCheckoutFeeApplied().compareTo(BigDecimal.ZERO) > 0);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        Invoice saved = captor.getValue();
        boolean hasChargeItem = saved.getItems().stream()
                .anyMatch(item -> item.getItemType() == InvoiceItemType.CHARGE);
        assertTrue(hasChargeItem);
        verify(roomAssignmentService).vacateAllReservationRooms(1L);
    }

    @Test
    void complete_shouldNotApplyLateFee_whenReceptionistOptsOut() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(1000));
        r.setCheckInAt(LocalDate.now().minusDays(3));
        r.setCheckOutAt(LocalDate.now().minusDays(1));
        r.setStatus(ReservationStatus.CHECKED_IN);

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(200));
        r.setDetails(List.of(detail));

        Payment deposit = new Payment();
        deposit.setAmount(BigDecimal.valueOf(100));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(systemConfigService.getConfig()).thenReturn(config);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(paymentRepository.findAllByReservationId(1L)).thenReturn(List.of(deposit));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(roomAssignmentService.getAssignedRoomNumbers(1L)).thenReturn("101");
        when(reservationRepository.save(any())).thenReturn(r);

        CheckoutForm form = new CheckoutForm(PaymentMethod.CASH, false);
        checkoutService.complete(1L, form, "user");

        assertTrue(r.getLateCheckoutFeeApplied() == null
                || r.getLateCheckoutFeeApplied().compareTo(BigDecimal.ZERO) == 0);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        Invoice saved = captor.getValue();
        boolean hasChargeItem = saved.getItems().stream()
                .anyMatch(item -> item.getItemType() == InvoiceItemType.CHARGE);
        assertFalse(hasChargeItem);
        verify(roomAssignmentService).vacateAllReservationRooms(1L);
    }
}
