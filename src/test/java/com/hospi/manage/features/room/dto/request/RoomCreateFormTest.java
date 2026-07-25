package com.hospi.manage.features.room.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoomCreateFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        var form = new RoomCreateForm((short) 1, 5, 1L);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenFloorNull() {
        var form = new RoomCreateForm(null, 5, 1L);
        Set<ConstraintViolation<RoomCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("floor", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenFloorBelowOne() {
        var form = new RoomCreateForm((short) 0, 5, 1L);
        Set<ConstraintViolation<RoomCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("floor", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenNumberOfRoomsNull() {
        var form = new RoomCreateForm((short) 1, null, 1L);
        Set<ConstraintViolation<RoomCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("numberOfRooms", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenNumberOfRoomsBelowOne() {
        var form = new RoomCreateForm((short) 1, 0, 1L);
        Set<ConstraintViolation<RoomCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("numberOfRooms", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenNumberOfRoomsAboveMax() {
        var form = new RoomCreateForm((short) 1, 100, 1L);
        Set<ConstraintViolation<RoomCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("numberOfRooms", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenRoomTypeIdNull() {
        var form = new RoomCreateForm((short) 1, 5, null);
        Set<ConstraintViolation<RoomCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("roomTypeId", violations.iterator().next().getPropertyPath().toString());
    }
}
