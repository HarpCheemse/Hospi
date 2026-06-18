package com.hospi.manage.features.room.validation;

import com.hospi.manage.features.room.dto.room.RoomCreateForm;
import com.hospi.manage.features.room.dto.room.RoomEditForm;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.repository.RoomRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class RoomValidator {
    private final RoomRepository roomRepository;

    public RoomValidator(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public void validateUpdate(Room room, RoomEditForm form, Errors errors) {
        if (errors.hasFieldErrors("roomNumber")) {
            return;
        }

        if (!isValidRoomNumber(room.getFloorNumber(),
                form.roomNumber())) {
            errors.rejectValue("roomNumber",
                    "invalid",
                    "Invalid room number");
        }

        if (!room.getRoomNumber().equals(form.roomNumber()) && roomNumberExists(form.roomNumber())) {
            errors.rejectValue("roomNumber",
                    "duplicate",
                    "Room already exists");
        }
    }

    public void validateCreate(RoomCreateForm form, Errors errors) {
        Integer highest =
                roomRepository.findHighestRoomNumberByFloor(form.floor());

        if (highest == null) {
            highest = form.floor() * 100;
        }

        int start = highest + 1;
        int end = highest + form.numberOfRooms() - 1;

        if (end / 100 != start / 100) {
            errors.rejectValue(
                    "numberOfRooms",
                    "capacity.exceeded",
                    "Floor " + form.floor() + " does not have enough room capacity"
            );
        }
    }

    private boolean roomNumberExists(String roomNumber) {
        return roomRepository.existsByRoomNumber(roomNumber);
    }

    private boolean isValidRoomNumber(short floor, String roomNumber) {
        int number = Integer.parseInt(roomNumber);
        return number / 100 == floor;
    }
}
