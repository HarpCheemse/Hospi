package com.hospi.manage.features.invoice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** View model for the manager revenue page. */
public record RevenueView(
        BigDecimal totalRevenue,
        int totalBookings,
        BigDecimal averagePerBooking,
        BigDecimal taxCollected,
        int totalRoomNights,
        BigDecimal previousPeriodRevenue,
        List<RevenueChartPoint> chartData,
        List<RoomTypeRevenue> roomTypeBreakdown,
        List<PaymentMethodRevenue> paymentBreakdown,
        List<BookingSourceRevenue> sourceBreakdown,
        List<DailyRevenuePoint> dailyChartData,
        LocalDate startDate,
        LocalDate endDate,
        String period
) {

    /** Compute the percentage change from the previous period. */
    public BigDecimal changePercent() {
        if (previousPeriodRevenue == null || previousPeriodRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return totalRevenue.subtract(previousPeriodRevenue)
                .multiply(BigDecimal.valueOf(100))
                .divide(previousPeriodRevenue, 1, java.math.RoundingMode.HALF_UP);
    }

    // -- Monthly chart accessors --

    public List<String> chartLabels() {
        return chartData.stream().map(RevenueChartPoint::label).toList();
    }

    public List<BigDecimal> chartRevenue() {
        return chartData.stream().map(RevenueChartPoint::revenue).toList();
    }

    public List<Integer> chartBookings() {
        return chartData.stream().map(RevenueChartPoint::bookings).toList();
    }

    // -- Room type breakdown accessors --

    public List<String> roomTypeLabels() {
        return roomTypeBreakdown.stream().map(RoomTypeRevenue::roomType).toList();
    }

    public List<BigDecimal> roomTypeRevenue() {
        return roomTypeBreakdown.stream().map(RoomTypeRevenue::revenue).toList();
    }

    // -- Payment method breakdown accessors --

    public List<String> paymentMethodLabels() {
        return paymentBreakdown.stream().map(PaymentMethodRevenue::method).toList();
    }

    public List<BigDecimal> paymentMethodRevenue() {
        return paymentBreakdown.stream().map(PaymentMethodRevenue::revenue).toList();
    }

    // -- Booking source breakdown accessors --

    public List<String> sourceLabels() {
        return sourceBreakdown.stream().map(BookingSourceRevenue::source).toList();
    }

    public List<BigDecimal> sourceRevenue() {
        return sourceBreakdown.stream().map(BookingSourceRevenue::revenue).toList();
    }

    // -- Daily chart accessors --

    public List<String> dailyLabels() {
        return dailyChartData.stream().map(DailyRevenuePoint::label).toList();
    }

    public List<BigDecimal> dailyRevenue() {
        return dailyChartData.stream().map(DailyRevenuePoint::revenue).toList();
    }

    public List<Integer> dailyBookings() {
        return dailyChartData.stream().map(DailyRevenuePoint::bookings).toList();
    }
}
