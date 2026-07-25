package com.hospi.manage.features.reservation.mapper;

import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManagerReservationMapperTest {

    // --- buildPaymentStatusMap ---

    @Test
    void shouldReturnPending_whenNoPayments() {
        var r = reservation(1L, BigDecimal.valueOf(500));
        var map = ManagerReservationMapper.buildPaymentStatusMap(List.of(r), List.of());
        assertEquals("PENDING", map.get(1L));
    }

    @Test
    void shouldReturnPaid_whenFullPayment() {
        var r = reservation(1L, BigDecimal.valueOf(500));
        var p = payment(1L, BigDecimal.valueOf(500), null);
        var map = ManagerReservationMapper.buildPaymentStatusMap(List.of(r), List.of(p));
        assertEquals("PAID", map.get(1L));
    }

    @Test
    void shouldReturnDeposit_whenPartialPayment() {
        var r = reservation(1L, BigDecimal.valueOf(500));
        var p = payment(1L, BigDecimal.valueOf(200), null);
        var map = ManagerReservationMapper.buildPaymentStatusMap(List.of(r), List.of(p));
        assertEquals("DEPOSIT", map.get(1L));
    }

    @Test
    void shouldReturnRefunded_whenRefunded() {
        var r = reservation(1L, BigDecimal.valueOf(500));
        var p = payment(1L, BigDecimal.valueOf(500), LocalDateTime.now());
        var map = ManagerReservationMapper.buildPaymentStatusMap(List.of(r), List.of(p));
        assertEquals("REFUNDED", map.get(1L));
    }

    @Test
    void shouldReturnEmptyMap_whenNoReservations() {
        var map = ManagerReservationMapper.buildPaymentStatusMap(List.of(), null);
        assertTrue(map.isEmpty());
    }

    // --- filterByScope ---

    @Test
    void shouldFilterToday() {
        var today = LocalDate.of(2026, 7, 13);
        var r1 = reservation(1L, today.minusDays(1), today.plusDays(2));
        var r2 = reservation(2L, today.minusDays(5), today.minusDays(1));
        var result = ManagerReservationMapper.filterByScope(List.of(r1, r2), "today", today);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldFilterUpcoming() {
        var today = LocalDate.of(2026, 7, 13);
        var r1 = reservation(1L, today.plusDays(1), today.plusDays(3));
        var r2 = reservation(2L, today.minusDays(1), today.plusDays(1));
        var result = ManagerReservationMapper.filterByScope(List.of(r1, r2), "upcoming", today);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldFilterHistory() {
        var today = LocalDate.of(2026, 7, 13);
        var r1 = reservation(1L, today.minusDays(5), today.minusDays(1));
        var r2 = reservation(2L, today.minusDays(1), today.plusDays(1));
        var result = ManagerReservationMapper.filterByScope(List.of(r1, r2), "history", today);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldReturnAll_whenScopeIsAll() {
        var today = LocalDate.of(2026, 7, 13);
        var r1 = reservation(1L, today.minusDays(5), today.minusDays(1));
        var r2 = reservation(2L, today.plusDays(1), today.plusDays(3));
        var result = ManagerReservationMapper.filterByScope(List.of(r1, r2), "all", today);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmpty_whenNullDates() {
        var today = LocalDate.of(2026, 7, 13);
        var r = new Reservation();
        r.setId(1L);
        var result = ManagerReservationMapper.filterByScope(List.of(r), "today", today);
        assertTrue(result.isEmpty());
    }

    // --- filterBySearch ---

    @Test
    void shouldFilterByName() {
        var r1 = reservation(1L);
        r1.setGuestName("Alice Johnson");
        var r2 = reservation(2L);
        r2.setGuestName("Bob Smith");
        var result = ManagerReservationMapper.filterBySearch(List.of(r1, r2), "alice");
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldFilterByEmail() {
        var r1 = reservation(1L);
        r1.setGuestEmail("alice@test.com");
        var r2 = reservation(2L);
        r2.setGuestEmail("bob@test.com");
        var result = ManagerReservationMapper.filterBySearch(List.of(r1, r2), "alice@");
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldFilterByPhone() {
        var r1 = reservation(1L);
        r1.setGuestPhone("+84 123");
        var r2 = reservation(2L);
        r2.setGuestPhone("+84 456");
        var result = ManagerReservationMapper.filterBySearch(List.of(r1, r2), "123");
        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterById() {
        var r1 = reservation(1L);
        var r2 = reservation(2L);
        var result = ManagerReservationMapper.filterBySearch(List.of(r1, r2), "2");
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void shouldReturnEmpty_whenNoMatch() {
        var r1 = reservation(1L);
        r1.setGuestName("Alice");
        var result = ManagerReservationMapper.filterBySearch(List.of(r1), "Bob");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAll_whenSearchBlank() {
        var r1 = reservation(1L);
        var r2 = reservation(2L);
        var result = ManagerReservationMapper.filterBySearch(List.of(r1, r2), "");
        assertEquals(2, result.size());
    }

    // --- paginate ---

    @Test
    void shouldPaginate() {
        var list = List.of(1, 2, 3, 4, 5);
        assertEquals(List.of(1, 2), ManagerReservationMapper.paginate(list, 0, 2));
        assertEquals(List.of(3, 4), ManagerReservationMapper.paginate(list, 1, 2));
        assertEquals(List.of(5), ManagerReservationMapper.paginate(list, 2, 2));
    }

    @Test
    void shouldReturnEmpty_whenPageOutOfRange() {
        assertTrue(ManagerReservationMapper.paginate(List.of(1), 10, 10).isEmpty());
    }

    // --- countByDate ---

    @Test
    void shouldCountArriving() {
        var date = LocalDate.of(2026, 7, 13);
        var r1 = reservation(1L, date, date.plusDays(2));
        var r2 = reservation(2L, date, date.plusDays(1));
        var r3 = reservation(3L, date.plusDays(1), date.plusDays(3));
        assertEquals(2, ManagerReservationMapper.countByDate(List.of(r1, r2, r3), date, "arriving"));
    }

    @Test
    void shouldCountDeparting() {
        var date = LocalDate.of(2026, 7, 15);
        var r1 = reservation(1L, date.minusDays(2), date);
        var r2 = reservation(2L, date.minusDays(1), date);
        assertEquals(2, ManagerReservationMapper.countByDate(List.of(r1, r2), date, "departing"));
    }

    @Test
    void shouldReturnZero_whenNoMatch() {
        var date = LocalDate.of(2026, 7, 13);
        var r = reservation(1L, date.plusDays(1), date.plusDays(3));
        assertEquals(0, ManagerReservationMapper.countByDate(List.of(r), date, "arriving"));
    }

    // --- helpers ---

    private Reservation reservation(Long id) {
        var r = new Reservation();
        r.setId(id);
        r.setGuestName("Guest " + id);
        return r;
    }

    private Reservation reservation(Long id, BigDecimal totalPrice) {
        var r = reservation(id);
        r.setTotalPrice(totalPrice);
        return r;
    }

    private Reservation reservation(Long id, LocalDate checkIn, LocalDate checkOut) {
        var r = reservation(id);
        r.setCheckInAt(checkIn);
        r.setCheckOutAt(checkOut);
        return r;
    }

    private Payment payment(Long id, BigDecimal amount, LocalDateTime refundedAt) {
        var p = new Payment();
        p.setId(id);
        p.setAmount(amount);
        p.setRefundedAt(refundedAt);
        var r = new Reservation();
        r.setId(1L);
        p.setReservation(r);
        return p;
    }
}
