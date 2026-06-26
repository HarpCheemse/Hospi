package com.hospi.manage.features.room.dto.request;

import com.hospi.manage.features.room.enums.ConditionStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoomEditFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        var form = new RoomEditForm("101", 1L, ConditionStatus.CLEAN);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenRoomNumberBlank() {
        var form = new RoomEditForm("", 1L, ConditionStatus.CLEAN);
        Set<ConstraintViolation<RoomEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("roomNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenRoomNumberNonNumeric() {
        var form = new RoomEditForm("ABC", 1L, ConditionStatus.CLEAN);
        Set<ConstraintViolation<RoomEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("roomNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenRoomTypeIdNull() {
        var form = new RoomEditForm("101", null, ConditionStatus.CLEAN);
        Set<ConstraintViolation<RoomEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("roomTypeId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenConditionStatusNull() {
        var form = new RoomEditForm("101", 1L, null);
        Set<ConstraintViolation<RoomEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("conditionStatus", violations.iterator().next().getPropertyPath().toString());
    }
}
