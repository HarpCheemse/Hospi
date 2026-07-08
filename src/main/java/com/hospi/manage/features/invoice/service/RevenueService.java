package com.hospi.manage.features.invoice.service;

import com.hospi.manage.features.invoice.dto.response.*;
import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.invoice.repository.InvoiceRepository;
import com.hospi.manage.features.payment.entity.Payment;
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

        BigDecimal totalRevenue = invoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxCollected = invoices.stream()
                .map(Invoice::getTaxAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalBookings = invoices.size();
        BigDecimal averagePerBooking = totalBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int totalRoomNights = invoices.stream()
                .flatMap(inv -> inv.getItems().stream())
                .filter(item -> item.getItemType() == InvoiceItemType.ROOM)
                .map(InvoiceItem::getQuantity)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate previousStart = startDate.minusDays(daysBetween);
        LocalDate previousEnd = startDate.minusDays(1);
        BigDecimal previousPeriodRevenue = computeRevenueBetween(previousStart, previousEnd);

        List<RevenueChartPoint> chartData = buildChartData(invoices);

        // -- Breakdowns --
        List<RoomTypeRevenue> roomTypeBreakdown = buildRoomTypeBreakdown(invoices, totalRevenue);
        List<PaymentMethodRevenue> paymentBreakdown = buildPaymentBreakdown(invoices, totalRevenue);
        List<BookingSourceRevenue> sourceBreakdown = buildSourceBreakdown(invoices, totalRevenue);

        // Daily chart: only for ranges < 62 days to keep the chart readable
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<DailyRevenuePoint> dailyData = totalDays < 62
                ? buildDailyData(invoices)
                : List.of();

        return new RevenueView(totalRevenue, totalBookings, averagePerBooking, taxCollected,
                totalRoomNights, previousPeriodRevenue, chartData, roomTypeBreakdown,
                paymentBreakdown, sourceBreakdown, dailyData, startDate, endDate, period);
    }

    private BigDecimal computeRevenueBetween(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) return BigDecimal.ZERO;
        List<Invoice> invoices = invoiceRepository
                .findByStatusAndCreatedAtBetween(InvoiceStatus.PAID,
                        start.atStartOfDay(), end.atTime(LocalTime.MAX));
        return invoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Resolve the date range from a period key. */
    public LocalDate[] resolveDateRange(String period, LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case "this-week" -> new LocalDate[]{
                    today.with(java.time.DayOfWeek.MONDAY), today};
            case "this-year" -> new LocalDate[]{
                    today.with(java.time.Month.JANUARY).withDayOfMonth(1), today};
            case "custom" -> new LocalDate[]{
                    customStart != null ? customStart : today.withDayOfMonth(1),
                    customEnd != null ? customEnd : today};
            default -> // "this-month"
                    new LocalDate[]{today.withDayOfMonth(1), today};
        };
    }

    // -- Monthly chart --

    private List<RevenueChartPoint> buildChartData(List<Invoice> invoices) {
        if (invoices.isEmpty()) return List.of();

        Map<YearMonth, List<Invoice>> byMonth = invoices.stream()
                .collect(Collectors.groupingBy(inv ->
                        YearMonth.from(inv.getCreatedAt().toLocalDate())));

        List<RevenueChartPoint> points = new ArrayList<>();
        for (var entry : byMonth.entrySet()) {
            BigDecimal rev = sumAmounts(entry.getValue());
            points.add(new RevenueChartPoint(
                    entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    rev, entry.getValue().size()));
        }
        points.sort(Comparator.comparing(p ->
                YearMonth.parse(p.label(), DateTimeFormatter.ofPattern("MMM yyyy"))));
        return points;
    }

    // -- Room type breakdown --

    private List<RoomTypeRevenue> buildRoomTypeBreakdown(List<Invoice> invoices, BigDecimal totalRevenue) {
        Map<String, List<InvoiceItem>> byType = new HashMap<>();
        for (var inv : invoices) {
            for (var item : inv.getItems()) {
                if (item.getItemType() == InvoiceItemType.ROOM && item.getDescription() != null) {
                    String type = item.getDescription().contains(" x")
                            ? item.getDescription().substring(0, item.getDescription().indexOf(" x"))
                            : item.getDescription();
                    byType.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
                }
            }
        }
        List<RoomTypeRevenue> result = new ArrayList<>();
        for (var entry : byType.entrySet()) {
            BigDecimal rev = entry.getValue().stream()
                    .map(InvoiceItem::getAmount).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int count = entry.getValue().size();
            BigDecimal pct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? rev.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            result.add(new RoomTypeRevenue(entry.getKey(), rev, count, pct));
        }
        result.sort((a, b) -> b.revenue().compareTo(a.revenue()));
        return result;
    }

    // -- Payment method breakdown --

    private List<PaymentMethodRevenue> buildPaymentBreakdown(List<Invoice> invoices, BigDecimal totalRevenue) {
        if (invoices.isEmpty()) return List.of();

        List<Long> bookingIds = invoices.stream()
                .map(Invoice::getBookingId).toList();
        List<Payment> payments = paymentRepository.findByReservationIdIn(bookingIds);

        Map<String, BigDecimal> byMethod = new HashMap<>();
        for (var p : payments) {
            if (p.getAmount() != null) {
                byMethod.merge(p.getPaymentMethod().name(), p.getAmount(), BigDecimal::add);
            }
        }
        List<PaymentMethodRevenue> result = new ArrayList<>();
        for (var entry : byMethod.entrySet()) {
            BigDecimal pct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            result.add(new PaymentMethodRevenue(entry.getKey(), entry.getValue(), pct));
        }
        result.sort((a, b) -> b.revenue().compareTo(a.revenue()));
        return result;
    }

    // -- Booking source breakdown --

    private List<BookingSourceRevenue> buildSourceBreakdown(List<Invoice> invoices, BigDecimal totalRevenue) {
        if (invoices.isEmpty()) return List.of();

        List<Long> bookingIds = invoices.stream()
                .map(Invoice::getBookingId).toList();
        List<Reservation> reservations = reservationRepository.findByIdIn(bookingIds);
        Map<Long, String> idToSource = reservations.stream()
                .collect(Collectors.toMap(Reservation::getId,
                        r -> r.getSource().name()));

        Map<String, BigDecimal> bySource = new HashMap<>();
        for (var inv : invoices) {
            String src = idToSource.get(inv.getBookingId());
            if (src != null && inv.getTotalAmount() != null) {
                bySource.merge(src, inv.getTotalAmount(), BigDecimal::add);
            }
        }
        List<BookingSourceRevenue> result = new ArrayList<>();
        for (var entry : bySource.entrySet()) {
            BigDecimal pct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            result.add(new BookingSourceRevenue(entry.getKey(), entry.getValue(), pct));
        }
        result.sort((a, b) -> b.revenue().compareTo(a.revenue()));
        return result;
    }

    // -- Daily chart data --

    private List<DailyRevenuePoint> buildDailyData(List<Invoice> invoices) {
        if (invoices.isEmpty()) return List.of();

        Map<LocalDate, List<Invoice>> byDay = invoices.stream()
                .collect(Collectors.groupingBy(inv ->
                        inv.getCreatedAt().toLocalDate()));

        List<DailyRevenuePoint> points = new ArrayList<>();
        for (var entry : byDay.entrySet()) {
            BigDecimal rev = sumAmounts(entry.getValue());
            points.add(new DailyRevenuePoint(
                    entry.getKey().format(DateTimeFormatter.ofPattern("MMM d")),
                    rev, entry.getValue().size()));
        }
        points.sort(Comparator.comparing(DailyRevenuePoint::label));
        return points;
    }

    private BigDecimal sumAmounts(List<Invoice> invoices) {
        return invoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
