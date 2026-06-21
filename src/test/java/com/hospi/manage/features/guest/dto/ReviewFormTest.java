package com.hospi.manage.features.guest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReviewFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenValid() {
        ReviewForm form = new ReviewForm();
        form.setRating(4);
        form.setReservationId(1L);

        Set<ConstraintViolation<ReviewForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenRatingNull() {
        ReviewForm form = new ReviewForm();
        form.setReservationId(1L);

        Set<ConstraintViolation<ReviewForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("rating", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenRatingBelowMin() {
        ReviewForm form = new ReviewForm();
        form.setRating(0);
        form.setReservationId(1L);

        Set<ConstraintViolation<ReviewForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("rating", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenRatingAboveMax() {
        ReviewForm form = new ReviewForm();
        form.setRating(6);
        form.setReservationId(1L);

        Set<ConstraintViolation<ReviewForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("rating", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenReservationIdNull() {
        ReviewForm form = new ReviewForm();
        form.setRating(4);

        Set<ConstraintViolation<ReviewForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("reservationId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenReservationIdZero() {
        ReviewForm form = new ReviewForm();
        form.setRating(4);
        form.setReservationId(0L);

        Set<ConstraintViolation<ReviewForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("reservationId", violations.iterator().next().getPropertyPath().toString());
    }
}
