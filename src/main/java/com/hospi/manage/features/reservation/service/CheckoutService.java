package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.hotel.repository.HotelRepository;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.dto.request.CheckoutForm;
import com.hospi.manage.features.reservation.dto.response.CheckoutView;
import com.hospi.manage.features.reservation.dto.response.ReservationSummaryView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Service for computing charges, creating invoices, and completing reservation checkout. */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    /** Pre-computed financial breakdown — room charges, tax, deposit, late fee, and room numbers. */
    record ChargesBreakdown(
            BigDecimal roomCharges,
            BigDecimal taxAmount,
            BigDecimal taxRate,
            BigDecimal totalCharges,
            BigDecimal depositPaid,
            BigDecimal lateFeeAmount,
            long hoursPast,
            BigDecimal lateFeePerHour,
            String hotelCheckOutTime,
            String roomNumbers
    ) {}

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SystemConfigService systemConfigService;
    private final InvoiceRepository invoiceRepository;
    private final RoomAssignmentService roomAssignmentService;
    private final HotelRepository hotelRepository;

    /**
     * Compute all charges for a reservation at the current moment.
     */
    private ChargesBreakdown calculateChargesBreakdown(Reservation reservation) {
        var config = systemConfigService.getConfig();
        var hotel = hotelRepository.findById(HotelConstants.HOTEL_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // Room charges
        BigDecimal roomCharges = reservation.getTotalPrice();
        BigDecimal taxAmount = config.getTaxRate() != null
                ? roomCharges.multiply(config.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalCharges = roomCharges.add(taxAmount);

        // Deposit (sum of existing payments)
        List<Payment> existingPayments = paymentRepository.findAllByReservationId(reservation.getId());
        BigDecimal depositPaid = existingPayments != null
                ? existingPayments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;

        // Late fee (only if past scheduled check-out)
        LocalTime hotelCheckOutTime = hotel.getCheckOutTime() != null ? hotel.getCheckOutTime() : LocalTime.of(12, 0);
        LocalDateTime scheduledCheckOut = reservation.getCheckOutAt().atTime(hotelCheckOutTime);
        LocalDateTime now = LocalDateTime.now();
        long hoursPast = 0;
        if (now.isAfter(scheduledCheckOut)) {
            hoursPast = (long) Math.ceil(Duration.between(scheduledCheckOut, now).toMinutes() / 60.0);
        }

        BigDecimal lateFeePerHour = config.getLateCheckoutFee();
        BigDecimal lateFeeAmount = BigDecimal.ZERO;
        if (hoursPast > 0 && lateFeePerHour != null && lateFeePerHour.compareTo(BigDecimal.ZERO) > 0) {
            lateFeeAmount = lateFeePerHour.multiply(BigDecimal.valueOf(hoursPast));
        }

        String roomNumbers = roomAssignmentService.getAssignedRoomNumbers(reservation.getId());

        BigDecimal taxRate = config.getTaxRate();
        return new ChargesBreakdown(
                roomCharges, taxAmount, taxRate, totalCharges, depositPaid,
                lateFeeAmount, hoursPast, lateFeePerHour,
                hotelCheckOutTime.toString(), roomNumbers);
    }

    /**
     * Build the checkout view model with charges for the receipt page.
     * Called on both GET and POST validation failure.
     */
    public CheckoutView buildCheckoutView(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        ChargesBreakdown b = calculateChargesBreakdown(reservation);
        boolean showLateFee = b.lateFeeAmount().compareTo(BigDecimal.ZERO) > 0;
        BigDecimal remainingDue = b.totalCharges().subtract(b.depositPaid());

        return new CheckoutView(
                ReservationSummaryView.from(reservation),
                b.roomCharges(),
                b.taxAmount(),
                b.totalCharges(),
                b.depositPaid(),
                b.lateFeeAmount(),
                BigDecimal.valueOf(b.hoursPast()),
                showLateFee,
                remainingDue,
                List.of(PaymentMethod.CASH, PaymentMethod.CARD),
                b.hotelCheckOutTime(),
                b.roomNumbers()
        );
    }

    /** Complete the checkout — record payment, create invoice, vacate rooms. */
    @Transactional
    public void complete(Long reservationId, CheckoutForm form, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Status guard: only CHECKED_IN reservations can be checked out
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Only CHECKED_IN reservations can be checked out");
        }

        // Duplicate guard: prevent double checkout
        if (invoiceRepository.findByBookingId(reservationId).isPresent()) {
            throw new IllegalStateException("Invoice already exists for reservation #" + reservationId);
        }

        // Recalculate charges
        ChargesBreakdown b = calculateChargesBreakdown(reservation);

        // Late fee: only if receptionist opted in AND guest is past check-out time
        BigDecimal appliedLateFee = (form.applyLateCheckoutFee() && b.hoursPast() > 0 && b.lateFeePerHour() != null)
                ? b.lateFeePerHour().multiply(BigDecimal.valueOf(b.hoursPast()))
                : BigDecimal.ZERO;

        BigDecimal remainingDue = b.totalCharges().subtract(b.depositPaid()).add(appliedLateFee);
        if (remainingDue.compareTo(BigDecimal.ZERO) < 0) {
            remainingDue = BigDecimal.ZERO;
        }

        // Record payment
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(remainingDue);
        payment.setPaymentMethod(form.paymentMethod());
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(username);
        paymentRepository.save(payment);

        // Create invoice with line items
        Invoice invoice = new Invoice();
        invoice.setBookingId(reservation.getId());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setBalanceDue(BigDecimal.ZERO);

        int nights = (int) ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt());
        invoice.setSubtotal(b.roomCharges());
        invoice.setTaxAmount(b.taxAmount());
        invoice.setDepositUsed(b.depositPaid());

        BigDecimal totalWithLateFee = b.roomCharges().add(b.taxAmount()).add(appliedLateFee);
        invoice.setTotalAmount(totalWithLateFee);

        // ROOM line items — one per reservation detail (room type x count)
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

        // DISCOUNT line item — deposits reduce the amount due
        if (b.depositPaid().compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem discountItem = new InvoiceItem();
            discountItem.setInvoice(invoice);
            discountItem.setItemType(InvoiceItemType.DISCOUNT);
            discountItem.setDescription("Deposit applied");
            discountItem.setQuantity(1);
            discountItem.setUnitPrice(b.depositPaid());
            discountItem.setAmount(b.depositPaid());
            invoice.getItems().add(discountItem);
        }

        // CHARGE line item — late checkout fee
        if (appliedLateFee.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem lateFeeItem = new InvoiceItem();
            lateFeeItem.setInvoice(invoice);
            lateFeeItem.setItemType(InvoiceItemType.CHARGE);
            lateFeeItem.setDescription("Late checkout fee (" + b.hoursPast() + "h x $" + b.lateFeePerHour() + ")");
            lateFeeItem.setQuantity(1);
            lateFeeItem.setUnitPrice(appliedLateFee);
            lateFeeItem.setAmount(appliedLateFee);
            invoice.getItems().add(lateFeeItem);
        }

        // TAX line item
        if (b.taxAmount().compareTo(BigDecimal.ZERO) > 0) {
            InvoiceItem taxItem = new InvoiceItem();
            taxItem.setInvoice(invoice);
            taxItem.setItemType(InvoiceItemType.TAX);
            taxItem.setDescription("Tax (" + b.taxRate() + "%)");
            taxItem.setQuantity(1);
            taxItem.setUnitPrice(b.taxAmount());
            taxItem.setAmount(b.taxAmount());
            invoice.getItems().add(taxItem);
        }

        invoiceRepository.save(invoice);

        // Transition to checked-out state
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setCheckedOutAt(LocalDateTime.now());
        reservation.setCheckedOutBy(username);
        reservation.setLateCheckoutFeeApplied(appliedLateFee);
        reservationRepository.save(reservation);

        // Vacate rooms
        roomAssignmentService.vacateAllReservationRooms(reservationId);
    }
}
