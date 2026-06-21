package com.hospi.manage.features.receptionist.service;

import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.receptionist.dto.CheckoutCalculation;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SystemConfigService systemConfigService;
    private final InvoiceRepository invoiceRepository;


    public CheckoutCalculation calculate(Reservation reservation, LocalDateTime actualCheckoutTime, int adultGuestCount) {
        var config = systemConfigService.getConfig();
        BigDecimal totalPrice = reservation.getTotalPrice();

        BigDecimal depositPaid = BigDecimal.ZERO;
        List<Payment> existingPayments = paymentRepository.findAllByReservationId(reservation.getId());
        if (existingPayments != null && !existingPayments.isEmpty()) {
            depositPaid = existingPayments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal remainingBalance = totalPrice.subtract(depositPaid);
        if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            remainingBalance = BigDecimal.ZERO;
        }

        boolean isLate = false;
        BigDecimal lateCheckoutFee = BigDecimal.ZERO;
        if (actualCheckoutTime != null) {
            LocalTime standardCheckoutTime = LocalTime.of(11, 0);
            LocalDateTime standardCheckout = LocalDateTime.of(reservation.getCheckOutAt(), standardCheckoutTime);
            isLate = actualCheckoutTime.isAfter(standardCheckout);
            if (isLate) {
                lateCheckoutFee = config.getLateCheckoutFee();
            }
        }

        int totalRoomCapacity = reservation.getDetails().stream()
                .mapToInt(d -> d.getRoomType().getMaxOccupancy() * d.getRoomCount())
                .sum();
        int extraGuestCount = Math.max(0, adultGuestCount - totalRoomCapacity);
        BigDecimal extraGuestFee = config.getExtraGuestFee().multiply(BigDecimal.valueOf(extraGuestCount));

        BigDecimal totalDue = remainingBalance.add(lateCheckoutFee).add(extraGuestFee);

        return new CheckoutCalculation(
                totalPrice, depositPaid, remainingBalance,
                lateCheckoutFee, extraGuestFee, totalDue,
                isLate, extraGuestCount
        );
    }

    @Transactional
    public Reservation complete(Long reservationId, CheckoutCalculation calc,
                                 PaymentMethod method, String principal, LocalDateTime actualCheckoutTime,
                                 boolean applyLateFee) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        List<Payment> payments = paymentRepository.findAllByReservationId(reservationId);

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only CHECKED_IN or CONFIRMED reservations can be checked out");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservation.setStatus(ReservationStatus.CHECKED_IN);
            reservation.setCheckedInAt(LocalDateTime.now());
            reservation.setCheckedInBy(principal);
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(calc.totalDue());
        payment.setPaymentMethod(method);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(principal);
        paymentRepository.save(payment);

        BigDecimal appliedLateFee = applyLateFee ? calc.lateCheckoutFee() : BigDecimal.ZERO;
        BigDecimal appliedExtraGuestFee = calc.extraGuestFee();

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setBookingId(reservation.getId());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setBalanceDue(BigDecimal.ZERO);

        var config = systemConfigService.getConfig();
        int nights = (int) ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt());
        BigDecimal subtotal = reservation.getTotalPrice();
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (config.getTaxRate() != null) {
            taxAmount = subtotal.multiply(config.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        BigDecimal depositUsed = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDepositUsed(depositUsed);
        invoice.setTotalAmount(calc.totalDue());

        // ROOM line items
        for (var detail : reservation.getDetails()) {
            InvoiceItem roomItem = new InvoiceItem();
            roomItem.setInvoice(invoice);
            roomItem.setItemType(InvoiceItemType.ROOM);
            roomItem.setDescription(detail.getRoomType().getName() + " x" + detail.getRoomCount());
            roomItem.setQuantity(nights);
            roomItem.setUnitPrice(detail.getBasePrice().multiply(BigDecimal.valueOf(detail.getRoomCount())));
            roomItem.setAmount(detail.getTotalPrice());
            invoice.getItems().add(roomItem);
        }

        // DISCOUNT for deposit
        if (depositUsed.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem discountItem = new InvoiceItem();
            discountItem.setInvoice(invoice);
            discountItem.setItemType(InvoiceItemType.DISCOUNT);
            discountItem.setDescription("Deposit applied");
            discountItem.setQuantity(1);
            discountItem.setUnitPrice(depositUsed);
            discountItem.setAmount(depositUsed);
            invoice.getItems().add(discountItem);
        }

        // CHARGE for late checkout fee
        if (appliedLateFee.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem lateFeeItem = new InvoiceItem();
            lateFeeItem.setInvoice(invoice);
            lateFeeItem.setItemType(InvoiceItemType.CHARGE);
            lateFeeItem.setDescription("Late checkout fee");
            lateFeeItem.setQuantity(1);
            lateFeeItem.setUnitPrice(appliedLateFee);
            lateFeeItem.setAmount(appliedLateFee);
            invoice.getItems().add(lateFeeItem);
        }

        // CHARGE for extra guest fee
        if (appliedExtraGuestFee.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem extraGuestItem = new InvoiceItem();
            extraGuestItem.setInvoice(invoice);
            extraGuestItem.setItemType(InvoiceItemType.CHARGE);
            extraGuestItem.setDescription("Extra guest fee");
            extraGuestItem.setQuantity(1);
            extraGuestItem.setUnitPrice(appliedExtraGuestFee);
            extraGuestItem.setAmount(appliedExtraGuestFee);
            invoice.getItems().add(extraGuestItem);
        }

        // TAX
        if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem taxItem = new InvoiceItem();
            taxItem.setInvoice(invoice);
            taxItem.setItemType(InvoiceItemType.TAX);
            taxItem.setDescription("Tax (" + config.getTaxRate() + "%)");
            taxItem.setQuantity(1);
            taxItem.setUnitPrice(taxAmount);
            taxItem.setAmount(taxAmount);
            invoice.getItems().add(taxItem);
        }

        invoiceRepository.save(invoice);

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setCheckedOutAt(LocalDateTime.now());
        reservation.setCheckedOutBy(principal);

        reservation.setLateCheckoutFeeApplied(appliedLateFee);
        reservation.setExtraGuestFeeApplied(appliedExtraGuestFee);

        return reservationRepository.save(reservation);
    }
}
