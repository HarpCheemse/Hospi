package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReceiptDetailView(
        Long bookingId,
        String guestName,
        String roomNumbers,
        BigDecimal roomCharges,
        BigDecimal taxAmount,
        BigDecimal depositPaid,
        BigDecimal lateCheckoutFee,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        InvoiceStatus status,
        List<ReceiptItemView> items,
        PaymentMethod paymentMethod,
        String checkedOutBy,
        LocalDateTime checkedOutAt,
        LocalDateTime createdAt
) {
    public static ReceiptDetailView from(Reservation reservation, Invoice invoice, List<Payment> payments, String roomNumbers) {
        BigDecimal amountPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentMethod method = payments.isEmpty() ? null : payments.get(payments.size() - 1).getPaymentMethod();

        List<ReceiptItemView> itemViews = invoice.getItems() != null
                ? invoice.getItems().stream()
                        .map(ReceiptItemView::from)
                        .toList()
                : List.of();

        BigDecimal lateCheckoutFee = reservation.getLateCheckoutFeeApplied() != null
                ? reservation.getLateCheckoutFeeApplied()
                : BigDecimal.ZERO;

        return new ReceiptDetailView(
                reservation.getId(),
                reservation.getGuestName(),
                roomNumbers,
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getDepositUsed(),
                lateCheckoutFee,
                invoice.getTotalAmount(),
                amountPaid,
                invoice.getStatus(),
                itemViews,
                method,
                reservation.getCheckedOutBy(),
                reservation.getCheckedOutAt(),
                invoice.getCreatedAt()
        );
    }
}
