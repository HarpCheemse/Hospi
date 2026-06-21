package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.manager.detail.repository.HotelRepository;
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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final RoomAssignmentRepository roomAssignmentRepository;
    private final StayingGuestRepository stayingGuestRepository;

    RoomService(RoomRepository roomRepository,
                RoomTypeRepository roomTypeRepository,
                HotelRepository hotelRepository,
                RoomAssignmentRepository roomAssignmentRepository,
                StayingGuestRepository stayingGuestRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.roomAssignmentRepository = roomAssignmentRepository;
        this.stayingGuestRepository = stayingGuestRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc();
    }

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
        return RoomMapper.toFloorView(floorNumber,
                rooms);
    }

    public List<FloorView> getFloorViews() {

        List<Room> allRooms = roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc();

        Map<Integer, List<Room>> roomsByFloor = allRooms.stream()
                .collect(Collectors.groupingBy(r -> (int) r.getFloorNumber()));

        return roomsByFloor.entrySet().stream()
                .map(e -> buildFloorView(e.getKey(),
                        e.getValue()))
                .toList();
    }

    public Room findById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Room")
        );
        if (!room.isActive()) {
            throw new ResourceNotFoundException("Room");
        }
        return room;
    }

    public void updateRoom(Long id, RoomEditForm form) {
        Room room = findById(id);

        RoomType roomType = roomTypeRepository.findById(form.roomTypeId())
                .filter(RoomType::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Room type"));

        room.setRoomType(roomType);
        room.setConditionStatus(form.conditionStatus());
        room.setRoomNumber(form.roomNumber());

        roomRepository.save(room);
    }

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
                .map(e -> buildFloorView(e.getKey(),
                        e.getValue()))
                .toList();
    }

    @Transactional
    public void updateConditionStatus(Long id, ConditionStatus status) {
        Room room = findById(id);
        room.setConditionStatus(status);
        roomRepository.save(room);
    }

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

    public RoomOccupancyView getRoomOccupancy(Long id) {
        Room room = findById(id);

        if (room.getOccupancyStatus() != OccupancyStatus.OCCUPIED) {
            return RoomOccupancyView.vacant(room);
        }

        List<RoomAssignment> assignments = roomAssignmentRepository
                .findByRoomIdAndReservation_StatusIn(id,
                        List.of(ReservationStatus.CHECKED_IN));

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
                reservation.getGuestName(),
                reservation.getCheckInAt(),
                reservation.getCheckOutAt(),
                stayingGuests.stream().map(GuestView::from).toList()
        );
    }
}
