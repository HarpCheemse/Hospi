package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.response.ReceiptDetailView;
import com.hospi.manage.features.reservation.dto.response.ReceiptItemView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private RoomAssignmentService roomAssignmentService;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void getReceiptDetail_shouldReturnViewWithInvoiceItems() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setGuestName("John Doe");
        reservation.setCheckedOutAt(LocalDateTime.now());
        reservation.setCheckedOutBy("Receptionist1");

        InvoiceItem roomItem = new InvoiceItem();
        roomItem.setItemType(InvoiceItemType.ROOM);
        roomItem.setDescription("Deluxe Room x3");
        roomItem.setQuantity(3);
        roomItem.setUnitPrice(BigDecimal.valueOf(200));
        roomItem.setAmount(BigDecimal.valueOf(600));

        InvoiceItem taxItem = new InvoiceItem();
        taxItem.setItemType(InvoiceItemType.TAX);
        taxItem.setDescription("Tax 10%");
        taxItem.setQuantity(1);
        taxItem.setUnitPrice(BigDecimal.valueOf(60));
        taxItem.setAmount(BigDecimal.valueOf(60));

        Invoice invoice = new Invoice();
        invoice.setId(10L);
        invoice.setBookingId(1L);
        invoice.setSubtotal(BigDecimal.valueOf(600));
        invoice.setTaxAmount(BigDecimal.valueOf(60));
        invoice.setDepositUsed(BigDecimal.valueOf(200));
        invoice.setTotalAmount(BigDecimal.valueOf(660));
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setItems(List.of(roomItem, taxItem));
        invoice.setCreatedAt(LocalDateTime.now());

        Payment payment = new Payment();
        payment.setId(100L);
        payment.setAmount(BigDecimal.valueOf(660));
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setConfirmedAt(LocalDateTime.now());

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findAllByReservationId(1L)).thenReturn(List.of(payment));
        when(roomAssignmentService.getAssignedRoomNumbers(1L)).thenReturn("101, 102");

        ReceiptDetailView view = receiptService.getReceiptDetail(1L);

        assertNotNull(view);
        assertEquals(1L, view.bookingId());
        assertEquals("John Doe", view.guestName());
        assertEquals("101, 102", view.roomNumbers());
        assertEquals(BigDecimal.valueOf(600), view.roomCharges());
        assertEquals(BigDecimal.valueOf(60), view.taxAmount());
        assertEquals(BigDecimal.valueOf(660), view.totalAmount());
        assertEquals(BigDecimal.valueOf(660), view.amountPaid());
        assertEquals(InvoiceStatus.PAID, view.status());
        assertEquals(2, view.items().size());

        ReceiptItemView expectedRoomItem = view.items().get(0);
        assertEquals(InvoiceItemType.ROOM, expectedRoomItem.itemType());
        assertEquals(3, expectedRoomItem.quantity());

        ReceiptItemView expectedTaxItem = view.items().get(1);
        assertEquals(InvoiceItemType.TAX, expectedTaxItem.itemType());
        assertEquals(BigDecimal.valueOf(60), expectedTaxItem.amount());
    }

    @Test
    void getReceiptDetail_shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> receiptService.getReceiptDetail(99L));
    }
}
