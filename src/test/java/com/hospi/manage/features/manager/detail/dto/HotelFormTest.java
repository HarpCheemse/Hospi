package com.hospi.manage.features.manager.detail.dto;

import com.hospi.manage.features.manager.detail.enums.HotelStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HotelFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static HotelForm validForm() {
        var form = new HotelForm();
        form.setName("Grand Hotel");
        form.setDescription("A luxury hotel");
        form.setAddress("123 Main St");
        form.setPhone("+1234567890");
        form.setEmail("info@grandhotel.com");
        form.setCheckInTime(LocalTime.of(14, 0));
        form.setCheckOutTime(LocalTime.of(11, 0));
        form.setFeatures("Pool, Gym, Spa");
        form.setStatus(HotelStatus.ACTIVE);
        return form;
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        assertTrue(validator.validate(validForm()).isEmpty());
    }

    @Test
    void shouldPass_whenOptionalFieldsNull() {
        var form = validForm();
        form.setDescription(null);
        form.setEmail(null);
        form.setFeatures(null);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenNameBlank() {
        var form = validForm();
        form.setName("");
        Set<ConstraintViolation<HotelForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenAddressBlank() {
        var form = validForm();
        form.setAddress("");
        Set<ConstraintViolation<HotelForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("address", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenPhoneBlank() {
        var form = validForm();
        form.setPhone("");
        Set<ConstraintViolation<HotelForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("phone", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        var form = validForm();
        form.setEmail("not-an-email");
        Set<ConstraintViolation<HotelForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenCheckInTimeNull() {
        var form = validForm();
        form.setCheckInTime(null);
        Set<ConstraintViolation<HotelForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("checkInTime", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenCheckOutTimeNull() {
        var form = validForm();
        form.setCheckOutTime(null);
        Set<ConstraintViolation<HotelForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("checkOutTime", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenStatusNull() {
        var form = validForm();
        form.setStatus(null);
        Set<ConstraintViolation<HotelForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("status", violations.iterator().next().getPropertyPath().toString());
    }

}
