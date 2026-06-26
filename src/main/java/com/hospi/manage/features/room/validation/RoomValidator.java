package com.hospi.manage.features.room.validation;

import com.hospi.manage.features.room.dto.request.RoomCreateForm;
import com.hospi.manage.features.room.dto.request.RoomEditForm;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.repository.RoomRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/** Validator for room create and edit forms ({@link RoomCreateForm}, {@link RoomEditForm}). */
@Component
public class RoomValidator {
    private final RoomRepository roomRepository;

    public RoomValidator(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Validate a room-edit form for number format and uniqueness.
     *
     * @param room   the existing room being edited
     * @param form   the edit form data
     * @param errors binding result to populate with validation failures
     */
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

    /**
     * Validate a room-creation form for floor capacity.
     *
     * @param form   the room creation form data
     * @param errors binding result to populate with validation failures
     */
    public void validateCreate(RoomCreateForm form, Errors errors) {
        if (form.floor() == null || errors.hasFieldErrors("floor")
                || form.numberOfRooms() == null || errors.hasFieldErrors("numberOfRooms")) {
            return;
        }

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
        return roomRepository.existsByRoomNumberAndActiveTrue(roomNumber);
    }

    private boolean isValidRoomNumber(short floor, String roomNumber) {
        int number = Integer.parseInt(roomNumber);
        return number / 100 == floor;
    }
}
