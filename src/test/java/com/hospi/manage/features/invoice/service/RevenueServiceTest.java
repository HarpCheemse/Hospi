package com.hospi.manage.features.invoice.service;

import com.hospi.manage.features.invoice.dto.response.RevenueView;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RevenueService revenueService;

    @Test
    void shouldReturnPopulatedView_whenDataExists() {
        InvoiceItem roomItem = new InvoiceItem();
        roomItem.setItemType(InvoiceItemType.ROOM);
        roomItem.setAmount(BigDecimal.valueOf(500));
        roomItem.setQuantity(3);
        roomItem.setDescription("DELUXE DOUBLE x1");

        Invoice invoice = new Invoice();
        invoice.setTotalAmount(BigDecimal.valueOf(500));
        invoice.setTaxAmount(BigDecimal.valueOf(50));
        invoice.setBookingId(1L);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setItems(List.of(roomItem));

        when(invoiceRepository.findWithItemsByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(invoice));

        when(invoiceRepository.findByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        RevenueView view = revenueService.getRevenueView(start, end, "2026");

        assertEquals(BigDecimal.valueOf(500), view.totalRevenue());
        assertEquals(BigDecimal.valueOf(50), view.taxCollected());
        assertEquals(1, view.totalBookings());
        assertEquals(3, view.totalRoomNights());
        assertEquals(0, view.previousPeriodRevenue().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldReturnZeroedView_whenNoData() {
        when(invoiceRepository.findWithItemsByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(invoiceRepository.findByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        RevenueView view = revenueService.getRevenueView(start, end, "2026");

        assertEquals(BigDecimal.ZERO, view.totalRevenue());
        assertEquals(BigDecimal.ZERO, view.taxCollected());
        assertEquals(0, view.totalBookings());
        assertEquals(BigDecimal.ZERO, view.averagePerBooking());
        assertEquals(0, view.totalRoomNights());
        assertTrue(view.chartData().isEmpty());
        assertTrue(view.roomTypeBreakdown().isEmpty());
    }

    @Test
    void shouldBuildRoomTypeBreakdown_whenRoomItemsExist() {
        InvoiceItem roomItem = new InvoiceItem();
        roomItem.setItemType(InvoiceItemType.ROOM);
        roomItem.setAmount(BigDecimal.valueOf(500));
        roomItem.setQuantity(2);
        roomItem.setDescription("DELUXE DOUBLE x1");

        Invoice invoice = new Invoice();
        invoice.setTotalAmount(BigDecimal.valueOf(500));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setBookingId(1L);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setItems(List.of(roomItem));

        when(invoiceRepository.findWithItemsByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(invoice));
        when(invoiceRepository.findByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        RevenueView view = revenueService.getRevenueView(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "2026");

        assertEquals(1, view.roomTypeBreakdown().size());
        assertEquals("DELUXE DOUBLE", view.roomTypeBreakdown().get(0).roomType());
        assertEquals(BigDecimal.valueOf(500), view.roomTypeBreakdown().get(0).revenue());
        assertEquals(1, view.roomTypeBreakdown().get(0).bookings());
    }

    @Test
    void shouldBuildPaymentBreakdown_whenPaymentsExist() {
        Invoice invoice = new Invoice();
        invoice.setTotalAmount(BigDecimal.valueOf(500));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setBookingId(1L);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setItems(List.of());

        Payment payment = new Payment();
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(BigDecimal.valueOf(500));

        when(invoiceRepository.findWithItemsByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(invoice));
        when(invoiceRepository.findByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(paymentRepository.findByReservationIdIn(List.of(1L)))
                .thenReturn(List.of(payment));

        RevenueView view = revenueService.getRevenueView(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "2026");

        assertEquals(1, view.paymentBreakdown().size());
        assertEquals("CASH", view.paymentBreakdown().get(0).method());
    }

    @Test
    void shouldBuildSourceBreakdown_whenReservationsExist() {
        Invoice invoice = new Invoice();
        invoice.setTotalAmount(BigDecimal.valueOf(500));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setBookingId(1L);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setItems(List.of());

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setSource(BookingSource.ONLINE);

        when(invoiceRepository.findWithItemsByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(invoice));
        when(invoiceRepository.findByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(reservationRepository.findByIdIn(List.of(1L)))
                .thenReturn(List.of(reservation));

        RevenueView view = revenueService.getRevenueView(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "2026");

        assertEquals(1, view.sourceBreakdown().size());
        assertEquals("ONLINE", view.sourceBreakdown().get(0).source());
    }

    @Test
    void shouldCompareToPreviousPeriod() {
        Invoice invoice = new Invoice();
        invoice.setTotalAmount(BigDecimal.valueOf(500));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setBookingId(1L);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setItems(List.of());

        when(invoiceRepository.findWithItemsByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(invoice));

        Invoice prevInvoice = new Invoice();
        prevInvoice.setTotalAmount(BigDecimal.valueOf(400));
        prevInvoice.setStatus(InvoiceStatus.PAID);
        prevInvoice.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(invoiceRepository.findByStatusAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(prevInvoice));

        RevenueView view = revenueService.getRevenueView(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "Jan 2026");

        assertEquals(BigDecimal.valueOf(400), view.previousPeriodRevenue());
        assertEquals(0, BigDecimal.valueOf(25).compareTo(view.changePercent()));
    }
}
