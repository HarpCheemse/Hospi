package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.response.ReceiptDetailView;
import com.hospi.manage.features.reservation.dto.response.TodayReceiptView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Service for receipt-related queries. */
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RoomAssignmentService roomAssignmentService;

    /** Get full receipt detail for a reservation. */
    public ReceiptDetailView getReceiptDetail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        Invoice invoice = invoiceRepository.findByBookingId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        List<Payment> payments = paymentRepository.findAllByReservationId(reservationId);
        String roomNumbers = roomAssignmentService.getAssignedRoomNumbers(reservationId);

        return ReceiptDetailView.from(reservation, invoice, payments, roomNumbers);
    }

    /** Get today's checked-out reservations as receipt summaries. */
    public List<TodayReceiptView> getTodayReceipts() {
        LocalDate today = LocalDate.now();
        var reservations = reservationRepository.findByStatusAndCheckedOutAtDate(
                ReservationStatus.CHECKED_OUT, today);

        return reservations.stream()
                .map(r -> {
                    var payments = paymentRepository.findAllByReservationId(r.getId());
                    BigDecimal amountPaid = payments.stream()
                            .map(p -> p.getAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalCharges = r.getTotalPrice();
                    if (r.getLateCheckoutFeeApplied() != null) {
                        totalCharges = totalCharges.add(r.getLateCheckoutFeeApplied());
                    }
                    return TodayReceiptView.from(r, totalCharges, amountPaid);
                })
                .toList();
    }
}
