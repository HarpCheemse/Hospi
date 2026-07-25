package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.utils.SecurityUtils;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.audit.enums.AuditAction;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import com.hospi.manage.features.reservation.repository.StayingGuestRepository;
import com.hospi.manage.features.room.dto.request.RoomCreateForm;
import com.hospi.manage.features.room.dto.request.RoomEditForm;
import com.hospi.manage.features.room.dto.response.FloorView;
import com.hospi.manage.features.room.dto.response.GuestView;
import com.hospi.manage.features.room.dto.response.RoomOccupancyView;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.mapper.RoomMapper;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Business logic for room CRUD, floor views, occupancy tracking, and soft-deletion. */
@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final NotificationService notificationService;
    private final RoomAssignmentRepository roomAssignmentRepository;
    private final StayingGuestRepository stayingGuestRepository;
    private final AuditService auditService;

    /** Return all active rooms ordered by floor and room number. */
    public List<Room> findAll() {
        return roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc();
    }

    public List<Room> getActiveDirtyRooms() {
        return roomRepository.findByConditionStatusAndActiveTrueOrderByFloorNumberAscRoomNumberAsc(ConditionStatus.DIRTY);
    }

    /** Create one or more rooms on a floor with auto-generated room numbers. */
    @Transactional
    public void createRoom(RoomCreateForm form) {
        Integer highest =
                roomRepository.findHighestRoomNumberByFloor(form.floor());
        if (highest == null) {
            highest = form.floor() * 100;
        }

        int start = highest + 1;

        RoomType roomType = roomTypeRepository.findById(form.roomTypeId())
                .filter(RoomType::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Room type"));

        List<Room> rooms = new ArrayList<>();

        for (int i = 0; i < form.numberOfRooms(); i++) {
            Room room = new Room();

            room.setRoomNumber(String.valueOf(start + i));
            room.setRoomType(roomType);
            room.setFloorNumber((short) form.floor());
            room.setConditionStatus(ConditionStatus.CLEAN);
            room.setOccupancyStatus(OccupancyStatus.VACANT);
            room.setActive(true);

            rooms.add(room);
        }

        roomRepository.saveAll(rooms);
    }


    private FloorView buildFloorView(int floorNumber, List<Room> rooms) {
        return RoomMapper.toFloorView(floorNumber, rooms);
    }

    /** Return floor-by-floor room views for all active rooms. */
    public List<FloorView> getFloorViews() {

        List<Room> allRooms = roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc();

        Map<Integer, List<Room>> roomsByFloor = allRooms.stream()
                .collect(Collectors.groupingBy(r -> (int) r.getFloorNumber()));

        return roomsByFloor.entrySet().stream()
                .map(e -> buildFloorView(e.getKey(),
                        e.getValue()))
                .toList();
    }

    /** Find an active room by ID. */
    public Room findById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Room")
        );
        if (!room.isActive()) {
            throw new ResourceNotFoundException("Room");
        }
        return room;
    }

    /** Update a room's type, number, and condition status. */
    @Transactional
    public void updateRoom(Long id, RoomEditForm form) {
        Room room = findById(id);

        ConditionStatus oldCondition = room.getConditionStatus();

        RoomType roomType = roomTypeRepository.findById(form.roomTypeId())
                .filter(RoomType::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found"));

        room.setRoomType(roomType);
        room.setConditionStatus(form.conditionStatus());
        room.setRoomNumber(form.roomNumber());

        roomRepository.save(room);

        if (oldCondition != ConditionStatus.CLEAN && form.conditionStatus() == ConditionStatus.CLEAN) {
            notificationService.notifyRole(
                    Role.RECEPTIONIST,
                    "Room Ready",
                    "Room " + room.getRoomNumber() + " is now clean and available"
            );
        }

        if (oldCondition == ConditionStatus.CLEAN && form.conditionStatus() == ConditionStatus.DIRTY) {
            notificationService.notifyRole(
                    Role.LEADER,
                    "Room Dirty",
                    "Room " + room.getRoomNumber() + " needs cleaning"
            );
        }

        if (oldCondition != form.conditionStatus()) {
            var action = switch (form.conditionStatus()) {
                case CLEAN -> AuditAction.CLEAN_ROOM;
                case DIRTY -> AuditAction.DIRTY_ROOM;
                case MAINTENANCE -> AuditAction.MAINTENANCE_ROOM;
            };
            auditService.log(
                    SecurityUtils.currentStaffId(),
                    SecurityUtils.currentStaffName(),
                    action,
                    "ROOM",
                    room.getId(),
                    "Room " + room.getRoomNumber() + " marked as " + form.conditionStatus().name().toLowerCase()
            );
        }
    }

    /** Return floor views optionally filtered by a specific floor number. */
    public List<FloorView> getFloorViews(Integer floor) {
        List<Room> allRooms = roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc();
        if (floor != null) {
            allRooms = allRooms.stream()
                    .filter(r -> r.getFloorNumber() == floor.shortValue())
                    .toList();
        }
        Map<Integer, List<Room>> roomsByFloor = allRooms.stream()
                .collect(Collectors.groupingBy(r -> (int) r.getFloorNumber()));

        return roomsByFloor.entrySet().stream()
                .map(e -> buildFloorView(e.getKey(), e.getValue()))
                .toList();
    }

    /** Update a room's condition status (clean, dirty, or maintenance). */
    @Transactional
    public void updateConditionStatus(Long id, ConditionStatus status) {
        Room room = findById(id);
        ConditionStatus oldCondition = room.getConditionStatus();
        room.setConditionStatus(status);
        roomRepository.save(room);

        if (oldCondition != ConditionStatus.CLEAN && status == ConditionStatus.CLEAN) {
            notificationService.notifyRole(
                    Role.RECEPTIONIST,
                    "Room Ready",
                    "Room " + room.getRoomNumber() + " is now clean and available"
            );
        }

        if (oldCondition == ConditionStatus.CLEAN && status == ConditionStatus.DIRTY) {
            notificationService.notifyRole(
                    Role.LEADER,
                    "Room Dirty",
                    "Room " + room.getRoomNumber() + " needs cleaning"
            );
        }

        var action = switch (status) {
            case CLEAN -> AuditAction.CLEAN_ROOM;
            case DIRTY -> AuditAction.DIRTY_ROOM;
            case MAINTENANCE -> AuditAction.MAINTENANCE_ROOM;
        };
        auditService.log(
                SecurityUtils.currentStaffId(),
                SecurityUtils.currentStaffName(),
                action,
                "ROOM",
                room.getId(),
                "Room " + room.getRoomNumber() + " marked as " + status.name().toLowerCase()
        );
    }

    /** Soft-delete a room by marking it inactive. */
    @Transactional
    public void delete(Long id) {
        Room room = findById(id);

        List<RoomAssignment> activeAssignments = roomAssignmentRepository
                .findByRoomIdAndReservation_StatusIn(id,
                        List.of(ReservationStatus.PENDING,
                                ReservationStatus.CONFIRMED,
                                ReservationStatus.CHECKED_IN));

        if (!activeAssignments.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete room " + room.getRoomNumber()
                            + " — it is referenced by active reservation(s)"
            );
        }

        room.setActive(false);
        roomRepository.save(room);
    }

    /** Return occupancy details for a room, including current guests if occupied. */
    public RoomOccupancyView getRoomOccupancy(Long id) {
        Room room = findById(id);

        if (room.getOccupancyStatus() != OccupancyStatus.OCCUPIED) {
            return RoomOccupancyView.vacant(room);
        }

        List<RoomAssignment> assignments = roomAssignmentRepository
                .findByRoomIdAndReservation_StatusIn(id, List.of(ReservationStatus.CHECKED_IN, ReservationStatus.CONFIRMED));

        if (assignments.isEmpty()) {
            return RoomOccupancyView.vacant(room);
        }

        Reservation reservation = assignments.get(0).getReservation();
        List<StayingGuest> stayingGuests = stayingGuestRepository
                .findByReservationIdOrderByCreatedAtAsc(reservation.getId());

        return new RoomOccupancyView(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType() != null ? room.getRoomType().getName() : null,
                OccupancyStatus.OCCUPIED,
                room.getConditionStatus(),
                reservation.getId(),
                reservation.getGuestName(),
                reservation.getCheckInAt(),
                reservation.getCheckOutAt(),
                stayingGuests.stream().map(GuestView::from).toList()
        );
    }


}
