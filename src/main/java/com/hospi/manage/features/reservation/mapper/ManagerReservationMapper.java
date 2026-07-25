package com.hospi.manage.features.reservation.mapper;

import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.reservation.entity.Reservation;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Static mapping utilities for the manager reservation list and detail views. */
public class ManagerReservationMapper {

    private ManagerReservationMapper() {}

    /** Build a map of reservation ID to payment status string. */
    public static Map<Long, String> buildPaymentStatusMap(
            List<Reservation> reservations,
            List<Payment> payments) {
        if (reservations.isEmpty()) return Map.of();

        Map<Long, List<Payment>> grouped = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getReservation().getId()));

        Map<Long, String> result = new HashMap<>();
        for (var r : reservations) {
            var pList = grouped.get(r.getId());
            if (pList == null || pList.isEmpty()) {
                result.put(r.getId(), "PENDING");
            } else {
                boolean anyRefunded = pList.stream().anyMatch(p -> p.getRefundedAt() != null);
                boolean anyDeposit = pList.stream().anyMatch(p ->
                        p.getAmount() != null && r.getTotalPrice() != null
                        && p.getAmount().compareTo(r.getTotalPrice()) < 0);
                if (anyRefunded) {
                    result.put(r.getId(), "REFUNDED");
                } else if (anyDeposit) {
                    result.put(r.getId(), "DEPOSIT");
                } else {
                    result.put(r.getId(), "PAID");
                }
            }
        }
        return result;
    }

    /** Filter reservations by time scope relative to today. */
    public static List<Reservation> filterByScope(List<Reservation> reservations, String scope, LocalDate today) {
        return reservations.stream()
                .filter(r -> {
                    if (r.getCheckInAt() == null || r.getCheckOutAt() == null) return false;
                    return switch (scope) {
                        case "today" -> !r.getCheckOutAt().isBefore(today) && !r.getCheckInAt().isAfter(today);
                        case "upcoming" -> r.getCheckInAt().isAfter(today);
                        case "history" -> r.getCheckOutAt().isBefore(today);
                        default -> true;
                    };
                })
                .toList();
    }

    /** Filter reservations by search query (name, email, phone, ID). */
    public static List<Reservation> filterBySearch(List<Reservation> reservations, String search) {
        String q = search.trim().toLowerCase();
        return reservations.stream()
                .filter(r -> (r.getGuestName() != null && r.getGuestName().toLowerCase().contains(q))
                        || (r.getGuestEmail() != null && r.getGuestEmail().toLowerCase().contains(q))
                        || (r.getGuestPhone() != null && r.getGuestPhone().contains(q))
                        || String.valueOf(r.getId()).equals(q))
                .toList();
    }

    /** Paginate a list manually. */
    public static <T> List<T> paginate(List<T> list, int page, int pageSize) {
        int start = page * pageSize;
        int end = Math.min(start + pageSize, list.size());
        return start >= list.size() ? List.of() : list.subList(start, end);
    }

    /** Count reservations arriving or departing on a given date. */
    public static long countByDate(List<Reservation> reservations, LocalDate date, String type) {
        return reservations.stream()
                .filter(r -> {
                    if (r.getCheckInAt() == null || r.getCheckOutAt() == null) return false;
                    return "arriving".equals(type) ? r.getCheckInAt().equals(date) : r.getCheckOutAt().equals(date);
                })
                .count();
    }
}
