package com.hospi.manage.features.reservation.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ManageReservationViewTest {

    private ReservationSummaryView makeSummary(LocalDate checkIn, LocalDate checkOut) {
        return new ReservationSummaryView(
                1L, "John", null, null, null, null,
                checkIn, checkOut, null, null, null, null, null, null, null, null, List.of());
    }

    @Test
    void shouldDelegateNightsToReservation() {
        var summary = makeSummary(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 4));
        var view = new ManageReservationView(summary, List.of(), List.of(), List.of(), Map.of());
        assertEquals(3, view.nights());
    }

    @Test
    void shouldReturnZeroAssignedCount_whenRoomTypeMissing() {
        var view = new ManageReservationView(
                makeSummary(LocalDate.now(), LocalDate.now().plusDays(1)),
                List.of(), List.of(), List.of(), Map.of());
        assertEquals(0, view.assignedCount(99L));
    }

    @Test
    void shouldReturnAssignedCount_whenRoomTypePresent() {
        var view = new ManageReservationView(
                makeSummary(LocalDate.now(), LocalDate.now().plusDays(1)),
                List.of(), List.of(), List.of(),
                Map.of(1L, 2, 3L, 1));
        assertEquals(2, view.assignedCount(1L));
        assertEquals(1, view.assignedCount(3L));
        assertEquals(0, view.assignedCount(99L));
    }
}
