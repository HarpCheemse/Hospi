package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.response.TodayReceiptView;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Service for receipt-related queries. */
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

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
