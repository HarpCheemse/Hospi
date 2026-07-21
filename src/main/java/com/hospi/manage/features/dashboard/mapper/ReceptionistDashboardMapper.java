package com.hospi.manage.features.dashboard.mapper;

import com.hospi.manage.features.dashboard.dto.QueueItemView;
import com.hospi.manage.features.dashboard.dto.ReceptionistDashboardView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Static mapping utilities for building the receptionist dashboard view model. */
public final class ReceptionistDashboardMapper {

    private ReceptionistDashboardMapper() {}

    /**
     * Build the receptionist dashboard view from service data.
     *
     * @param today                       current date
     * @param confirmed                   list of confirmed reservations
     * @param checkedIn                   list of checked-in reservations
     * @param checkedOutToday             list of reservations checked out today
     * @param allRooms                    list of all active rooms
     * @param assignmentsByReservationId  map of reservation ID to its room assignments
     * @param paymentsByReservationId     map of reservation ID to total payment amount
     * @return receptionist dashboard view model
     */
    public static ReceptionistDashboardView toView(
            LocalDate today,
            List<Reservation> confirmed,
            List<Reservation> checkedIn,
            List<Reservation> checkedOutToday,
            List<Room> allRooms,
            Map<Long, List<RoomAssignment>> assignmentsByReservationId,
            Map<Long, BigDecimal> paymentsByReservationId,
            Map<Long, Integer> guestCountsByReservationId
    ) {
        List<QueueItemView> todayQueue = new ArrayList<>();

        // 1. Arrivals Today (Check-In)
        for (Reservation res : confirmed) {
            if (today.equals(res.getCheckInAt())) {
                todayQueue.add(toQueueItem(res, "Check-In", "14:00",
                        assignmentsByReservationId, paymentsByReservationId,
                        guestCountsByReservationId));
            }
        }

        // 2. Departures Today (Check-Out)
        for (Reservation res : checkedIn) {
            if (today.equals(res.getCheckOutAt())) {
                todayQueue.add(toQueueItem(res, "Check-Out", "12:00",
                        assignmentsByReservationId, paymentsByReservationId,
                        guestCountsByReservationId));
            }
        }

        // Stats: Arrivals Today (Check-In)
        long arrivalsToday = confirmed.stream().filter(res -> today.equals(res.getCheckInAt())).count()
                + checkedIn.stream().filter(res -> today.equals(res.getCheckInAt())).count()
                + checkedOutToday.stream().filter(res -> today.equals(res.getCheckInAt())).count();

        long checkedInToday = checkedIn.stream().filter(res -> today.equals(res.getCheckInAt())).count()
                + checkedOutToday.stream().filter(res -> today.equals(res.getCheckInAt())).count();

        // Stats: Departures Today (Check-Out)
        long departuresToday = checkedIn.stream().filter(res -> today.equals(res.getCheckOutAt())).count()
                + checkedOutToday.stream().filter(res -> today.equals(res.getCheckOutAt())).count();

        long waitingCheckoutToday = checkedIn.stream().filter(res -> today.equals(res.getCheckOutAt())).count();

        // Room readiness counts
        int cleanedCount = (int) allRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.CLEAN)
                .count();

        int dirtyCount = (int) allRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.DIRTY)
                .count();

        int maintenanceCount = (int) allRooms.stream()
                .filter(r -> r.getConditionStatus() == ConditionStatus.MAINTENANCE)
                .count();

        int inspectionCount = 0;

        int availableRooms = (int) allRooms.stream()
                .filter(r -> r.getOccupancyStatus() == OccupancyStatus.VACANT)
                .count();

        int cleanedReadyRooms = (int) allRooms.stream()
                .filter(r -> r.getOccupancyStatus() == OccupancyStatus.VACANT && r.getConditionStatus() == ConditionStatus.CLEAN)
                .count();

        // Pending payments due today (arrivals and departures)
        BigDecimal pendingPaymentsAmount = BigDecimal.ZERO;
        int invoicesNeedFollowUp = 0;

        List<Reservation> todayReservations = new ArrayList<>();
        for (Reservation res : confirmed) {
            if (today.equals(res.getCheckInAt())) {
                todayReservations.add(res);
            }
        }
        for (Reservation res : checkedIn) {
            if (today.equals(res.getCheckOutAt())) {
                todayReservations.add(res);
            }
        }
        for (Reservation res : checkedOutToday) {
            if (today.equals(res.getCheckInAt()) || today.equals(res.getCheckOutAt())) {
                todayReservations.add(res);
            }
        }

        for (Reservation res : todayReservations) {
            BigDecimal paid = paymentsByReservationId.getOrDefault(res.getId(), BigDecimal.ZERO);
            BigDecimal balance = res.getTotalPrice().subtract(paid);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                pendingPaymentsAmount = pendingPaymentsAmount.add(balance);
                invoicesNeedFollowUp++;
            }
        }

        return new ReceptionistDashboardView(
                todayQueue,
                cleanedCount,
                dirtyCount,
                maintenanceCount,
                inspectionCount,
                arrivalsToday,
                checkedInToday,
                departuresToday,
                waitingCheckoutToday,
                availableRooms,
                cleanedReadyRooms,
                pendingPaymentsAmount,
                invoicesNeedFollowUp
        );
    }

    private static QueueItemView toQueueItem(
            Reservation res,
            String actionType,
            String time,
            Map<Long, List<RoomAssignment>> assignmentsByReservationId,
            Map<Long, BigDecimal> paymentsByReservationId,
            Map<Long, Integer> guestCountsByReservationId
    ) {
        String rooms = getRoomNumbers(res.getId(), assignmentsByReservationId);
        String roomTypeNames = getRoomTypeNames(res);
        int guestCount = guestCountsByReservationId.getOrDefault(res.getId(), 0);
        String paymentStatus = getPaymentStatus(res, paymentsByReservationId);
        return new QueueItemView(
                res.getId(),
                res.getGuestName(),
                res.getConfirmationCode(),
                rooms,
                roomTypeNames,
                guestCount,
                time,
                actionType,
                paymentStatus
        );
    }

    private static String getRoomTypeNames(Reservation res) {
        List<ReservationDetail> details = res.getDetails();
        if (details == null || details.isEmpty()) {
            return "";
        }
        return details.stream()
                .map(d -> d.getRoomType().getName())
                .collect(Collectors.joining(", "));
    }

    private static String getPaymentStatus(Reservation res, Map<Long, BigDecimal> paymentsByReservationId) {
        BigDecimal total = res.getTotalPrice();
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return "Pending";
        }
        BigDecimal paid = paymentsByReservationId.getOrDefault(res.getId(), BigDecimal.ZERO);
        if (paid.compareTo(BigDecimal.ZERO) <= 0) {
            return "Pending";
        }
        if (paid.compareTo(total) >= 0) {
            return "Paid";
        }
        return "Partial";
    }

    private static String getRoomNumbers(Long reservationId, Map<Long, List<RoomAssignment>> assignmentsByReservationId) {
        List<RoomAssignment> list = assignmentsByReservationId.get(reservationId);
        if (list == null || list.isEmpty()) {
            return "TBD";
        }
        return list.stream()
                .map(a -> a.getRoom().getRoomNumber())
                .collect(Collectors.joining(", "));
    }
}
