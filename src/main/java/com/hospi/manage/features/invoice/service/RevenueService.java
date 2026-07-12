package com.hospi.manage.features.invoice.service;

import com.hospi.manage.features.invoice.dto.response.*;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/** Business logic for revenue aggregation and reporting. */
@Service
@RequiredArgsConstructor
public class RevenueService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    /** Build the revenue view for the given date range and period label. */
    public RevenueView getRevenueView(LocalDate startDate, LocalDate endDate, String period) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Invoice> invoices = invoiceRepository
                .findWithItemsByStatusAndCreatedAtBetween(InvoiceStatus.PAID, start, end);

        BigDecimal totalRevenue = sumAmounts(invoices);
        BigDecimal taxCollected = sumTax(invoices);
        int totalBookings = invoices.size();
        BigDecimal averagePerBooking = totalBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        int totalRoomNights = sumRoomNights(invoices);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal previousPeriodRevenue = computeRevenueBetween(
                startDate.minusDays(daysBetween), startDate.minusDays(1));

        List<RevenueChartPoint> chartData = buildChartData(invoices);
        List<RoomTypeRevenue> roomTypeBreakdown = buildRoomTypeBreakdown(invoices, totalRevenue);
        List<PaymentMethodRevenue> paymentBreakdown = buildPaymentBreakdown(invoices, totalRevenue);
        List<BookingSourceRevenue> sourceBreakdown = buildSourceBreakdown(invoices, totalRevenue);

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<DailyRevenuePoint> dailyData = totalDays < 62 ? buildDailyData(invoices) : List.of();

        return new RevenueView(totalRevenue, totalBookings, averagePerBooking, taxCollected,
                totalRoomNights, previousPeriodRevenue, chartData, roomTypeBreakdown,
                paymentBreakdown, sourceBreakdown, dailyData, startDate, endDate, period);
    }

    private BigDecimal computeRevenueBetween(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) return BigDecimal.ZERO;
        return sumAmounts(invoiceRepository.findByStatusAndCreatedAtBetween(
                InvoiceStatus.PAID, start.atStartOfDay(), end.atTime(LocalTime.MAX)));
    }

    // -- Helpers --

    private BigDecimal sumAmounts(List<Invoice> invoices) {
        return invoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumTax(List<Invoice> invoices) {
        return invoices.stream()
                .map(Invoice::getTaxAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int sumRoomNights(List<Invoice> invoices) {
        return invoices.stream()
                .flatMap(inv -> inv.getItems().stream())
                .filter(item -> item.getItemType() == InvoiceItemType.ROOM)
                .map(InvoiceItem::getQuantity)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
    }

    private static BigDecimal computePercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }

    // -- Monthly chart --

    private List<RevenueChartPoint> buildChartData(List<Invoice> invoices) {
        if (invoices.isEmpty()) return List.of();

        Map<YearMonth, List<Invoice>> byMonth = invoices.stream()
                .collect(Collectors.groupingBy(inv ->
                        YearMonth.from(inv.getCreatedAt().toLocalDate())));

        return byMonth.entrySet().stream()
                .map(e -> new RevenueChartPoint(
                        e.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        sumAmounts(e.getValue()), e.getValue().size()))
                .sorted(Comparator.comparing(p ->
                        YearMonth.parse(p.label(), DateTimeFormatter.ofPattern("MMM yyyy"))))
                .toList();
    }

    // -- Room type breakdown --

    private List<RoomTypeRevenue> buildRoomTypeBreakdown(List<Invoice> invoices, BigDecimal totalRevenue) {
        Map<String, List<InvoiceItem>> byType = new HashMap<>();
        for (var inv : invoices) {
            for (var item : inv.getItems()) {
                if (item.getItemType() != InvoiceItemType.ROOM || item.getDescription() == null) continue;
                String type = item.getDescription().contains(" x")
                        ? item.getDescription().substring(0, item.getDescription().indexOf(" x"))
                        : item.getDescription();
                byType.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
            }
        }
        return byType.entrySet().stream()
                .map(e -> {
                    BigDecimal rev = e.getValue().stream()
                            .map(InvoiceItem::getAmount).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new RoomTypeRevenue(e.getKey(), rev, e.getValue().size(),
                            computePercentage(rev, totalRevenue));
                })
                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                .toList();
    }

    // -- Payment method breakdown --

    private List<PaymentMethodRevenue> buildPaymentBreakdown(List<Invoice> invoices, BigDecimal totalRevenue) {
        if (invoices.isEmpty()) return List.of();

        Map<String, BigDecimal> byMethod = new HashMap<>();
        for (var p : paymentRepository
                .findByReservationIdIn(invoices.stream().map(Invoice::getBookingId).toList())) {
            if (p.getAmount() != null) {
                byMethod.merge(p.getPaymentMethod().name(), p.getAmount(), BigDecimal::add);
            }
        }

        return byMethod.entrySet().stream()
                .map(e -> new PaymentMethodRevenue(e.getKey(), e.getValue(),
                        computePercentage(e.getValue(), totalRevenue)))
                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                .toList();
    }

    // -- Booking source breakdown --

    private List<BookingSourceRevenue> buildSourceBreakdown(List<Invoice> invoices, BigDecimal totalRevenue) {
        if (invoices.isEmpty()) return List.of();

        Map<Long, String> idToSource = reservationRepository
                .findByIdIn(invoices.stream().map(Invoice::getBookingId).toList())
                .stream()
                .collect(Collectors.toMap(Reservation::getId, r -> r.getSource().name()));

        Map<String, BigDecimal> bySource = new HashMap<>();
        for (var inv : invoices) {
            String src = idToSource.get(inv.getBookingId());
            if (src != null && inv.getTotalAmount() != null) {
                bySource.merge(src, inv.getTotalAmount(), BigDecimal::add);
            }
        }

        return bySource.entrySet().stream()
                .map(e -> new BookingSourceRevenue(e.getKey(), e.getValue(),
                        computePercentage(e.getValue(), totalRevenue)))
                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                .toList();
    }

    // -- Daily chart data --

    private List<DailyRevenuePoint> buildDailyData(List<Invoice> invoices) {
        if (invoices.isEmpty()) return List.of();

        return invoices.stream()
                .collect(Collectors.groupingBy(inv -> inv.getCreatedAt().toLocalDate()))
                .entrySet().stream()
                .map(e -> new DailyRevenuePoint(
                        e.getKey().format(DateTimeFormatter.ofPattern("MMM d")),
                        sumAmounts(e.getValue()), e.getValue().size()))
                .sorted(Comparator.comparing(DailyRevenuePoint::label))
                .toList();
    }
}
