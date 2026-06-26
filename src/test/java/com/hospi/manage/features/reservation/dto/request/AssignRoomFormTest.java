package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignRoomFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenRoomIdValid() {
        var form = new AssignRoomForm(1L);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenRoomIdNull() {
        var form = new AssignRoomForm(null);
        Set<ConstraintViolation<AssignRoomForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("roomId", violations.iterator().next().getPropertyPath().toString());
    }
}
