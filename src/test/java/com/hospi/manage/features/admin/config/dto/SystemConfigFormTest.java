package com.hospi.manage.features.admin.config.dto;

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
    void shouldPass_whenValidForm() {

        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("50.00"),
                new BigDecimal("15.00"),
                30,
                24,
                30,
                5,
                new BigDecimal("50.00"),
                24
        );

        Set<ConstraintViolation<SystemConfigForm>> violations =
                validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenAllFieldsAreInvalid() {

        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("-1"),
                new BigDecimal("200"),   // >100 invalid
                new BigDecimal("-10"),
                new BigDecimal("-5"),
                0,
                -1,
                0,
                0,
                new BigDecimal("200"),   // >100 invalid
                -1
        );

        Set<ConstraintViolation<SystemConfigForm>> violations =
                validator.validate(form);

        assertFalse(violations.isEmpty());

        violations.forEach(v ->
                System.out.println(v.getPropertyPath() + " -> " + v.getMessage())
        );
    }

    @Test
    void shouldFail_whenNullValues() {

        SystemConfigForm form = new SystemConfigForm(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<SystemConfigForm>> violations =
                validator.validate(form);

        assertEquals(10, violations.size());
    }
}
