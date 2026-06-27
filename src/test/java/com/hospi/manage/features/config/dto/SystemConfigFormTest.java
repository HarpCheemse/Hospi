package com.hospi.manage.features.config.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SystemConfigFormTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("50.00"),
                new BigDecimal("15.00"), 30, 24, 30, 5,
                new BigDecimal("50.00"), 24);

        Set<ConstraintViolation<SystemConfigForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenTaxRateNull() {
        SystemConfigForm form = new SystemConfigForm(
                null, new BigDecimal("20.00"), new BigDecimal("50.00"),
                new BigDecimal("15.00"), 30, 24, 30, 5,
                new BigDecimal("50.00"), 24);

        Set<ConstraintViolation<SystemConfigForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("taxRate", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenDefaultDepositPercentageNegative() {
        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("10.00"), new BigDecimal("-1.00"), new BigDecimal("50.00"),
                new BigDecimal("15.00"), 30, 24, 30, 5,
                new BigDecimal("50.00"), 24);

        Set<ConstraintViolation<SystemConfigForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFail_whenDefaultDepositPercentageOver100() {
        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("10.00"), new BigDecimal("101.00"), new BigDecimal("50.00"),
                new BigDecimal("15.00"), 30, 24, 30, 5,
                new BigDecimal("50.00"), 24);

        Set<ConstraintViolation<SystemConfigForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFail_whenMaxBookingDaysNull() {
        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("50.00"),
                new BigDecimal("15.00"), null, 24, null, 5,
                new BigDecimal("50.00"), 24);

        Set<ConstraintViolation<SystemConfigForm>> violations = validator.validate(form);

        assertEquals(2, violations.size());
    }

    @Test
    void shouldFail_whenMaxRoomPerBookLessThan1() {
        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("50.00"),
                new BigDecimal("15.00"), 30, 24, 30, 0,
                new BigDecimal("50.00"), 24);

        Set<ConstraintViolation<SystemConfigForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
    }
}
