package com.hospi.manage.features.room.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.repository.HotelRepository;
import com.hospi.manage.features.room.dto.FloorView;
import com.hospi.manage.features.room.dto.RoomCreateForm;
import com.hospi.manage.features.room.dto.RoomEditForm;
import com.hospi.manage.features.room.dto.RoomView;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;

    RoomService(RoomRepository roomRepository,
                RoomTypeRepository roomTypeRepository,
                HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Transactional
    public void createRoom(RoomCreateForm form) {
        Integer highest =
                roomRepository.findHighestRoomNumberByFloor(form.floor());
        if (highest == null) {
            highest = form.floor() * 100;
        }

        int start = highest + 1;

        RoomType roomType = roomTypeRepository.findById(form.roomTypeId()).orElseThrow(
                () -> new ResourceNotFoundException("Room type not found")
        );

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

        List<RoomView> roomViews = rooms.stream()
                .map(room -> new RoomView(
                        room.getId(),
                        room.getRoomNumber(),
                        room.getRoomType() != null
                                ? room.getRoomType().getName()
                                : null,
                        room.getOccupancyStatus(),
                        room.getConditionStatus()
                ))
                .toList();

        return new FloorView(
                floorNumber,
                roomViews.size(),
                roomViews
        );
    }

    public List<FloorView> getFloorViews() {

        Hotel hotel = hotelRepository.findById(HotelConstants.HOTEL_ID).orElseThrow(
                () -> new ResourceNotFoundException("hotel not found")
        );

        List<FloorView> floors = new ArrayList<>();

        for (int floor = 1; floor <= hotel.getFloorCount(); floor++) {

            List<Room> rooms = roomRepository.findByFloorNumberOrderByRoomNumber(floor);

            floors.add(buildFloorView(floor,
                    rooms));
        }

        return floors;
    }

    public Room findById(Long id) {
        return roomRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Room not found")
        );
    }

    public void updateRoom(Long id, RoomEditForm form) {
        Room room = findById(id);

        RoomType roomType =
                roomTypeRepository.findById(form.roomTypeId()).orElseThrow(
                        () -> new ResourceNotFoundException("Room type not found")
                );

        room.setRoomType(roomType);
        room.setConditionStatus(form.conditionStatus());
        room.setRoomNumber(form.roomNumber());

        roomRepository.save(room);
    }

    //TODO optimize this
    public boolean generateDefaultRoomLayout() {
        if (!roomRepository.findAll().isEmpty()) {
            return false;
        }
        Hotel hotel = hotelRepository.findById(HotelConstants.HOTEL_ID).orElseThrow(
                () -> new ResourceNotFoundException("Hotel")
        );
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        List<Room> rooms = new ArrayList<>();
        for (short floor = 1; floor <= hotel.getFloorCount(); floor++) {
            int number = floor * 100;
            for (RoomType roomType : roomTypes) {
                for (int i = 1; i <= 5; i++) {
                    number++;
                    Room room = new Room();

                    room.setRoomType(roomType);
                    room.setFloorNumber(floor);
                    room.setOccupancyStatus(OccupancyStatus.VACANT);
                    room.setConditionStatus(ConditionStatus.CLEAN);
                    room.setActive(true);
                    room.setRoomNumber(String.valueOf(number));

                    rooms.add(room);
                }
            }
        }
        roomRepository.saveAll(rooms);
        return true;
    }
}
