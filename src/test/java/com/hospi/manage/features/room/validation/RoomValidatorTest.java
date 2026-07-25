package com.hospi.manage.features.room.validation;

import com.hospi.manage.features.room.dto.request.RoomCreateForm;
import com.hospi.manage.features.room.dto.request.RoomEditForm;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomValidatorTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomValidator roomValidator;

    @Test
    void shouldReject_whenRoomNumberDoesNotMatchFloor() {
        Room room = new Room();
        room.setFloorNumber((short) 2);
        room.setRoomNumber("201");

        RoomEditForm form = new RoomEditForm("101",
                1L,
                ConditionStatus.CLEAN);

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        roomValidator.validateUpdate(room, form, errors);

        assertTrue(errors.hasFieldErrors("roomNumber"));
    }

    @Test
    void shouldReject_whenRoomNumberAlreadyExists() {
        Room room = new Room();
        room.setRoomNumber("102");
        room.setFloorNumber((short) 1);

        RoomEditForm form = new RoomEditForm("101",
                1L,
                ConditionStatus.CLEAN);

        when(roomRepository.existsByRoomNumberAndActiveTrue("101"))
                .thenReturn(true);

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        roomValidator.validateUpdate(room, form, errors);

        assertTrue(errors.hasFieldErrors("roomNumber"));
    }

    @Test
    void shouldReject_whenFloorCapacityExceeded() {
        when(roomRepository.findHighestRoomNumberByFloor((short) 1))
                .thenReturn(198);

        RoomCreateForm form = new RoomCreateForm((short) 1,
                3,
                1L);

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        roomValidator.validateCreate(form, errors);

        assertTrue(errors.hasFieldErrors("numberOfRooms"));
    }

    @Test
    void shouldPass_whenRoomNumberMatchesFloor() {
        Room room = new Room();
        room.setFloorNumber((short) 1);
        room.setRoomNumber("101");

        RoomEditForm form = new RoomEditForm("101", 1L, ConditionStatus.CLEAN);

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        roomValidator.validateUpdate(room, form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void shouldPass_whenCreatingWithinCapacity() {
        when(roomRepository.findHighestRoomNumberByFloor((short) 1)).thenReturn(50);

        RoomCreateForm form = new RoomCreateForm((short) 1, 3, 1L);

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        roomValidator.validateCreate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void shouldSkipValidation_whenExistingFieldErrors() {
        Room room = new Room();
        room.setFloorNumber((short) 1);
        room.setRoomNumber("101");

        RoomEditForm form = new RoomEditForm("101", 1L, ConditionStatus.CLEAN);

        BindingResult errors = new BeanPropertyBindingResult(form, "form");
        errors.rejectValue("roomNumber", "existing", "Pre-existing error");

        roomValidator.validateUpdate(room, form, errors);

        assertTrue(errors.hasFieldErrors("roomNumber"));
        assertEquals(1, errors.getFieldErrorCount("roomNumber"));
    }
}
