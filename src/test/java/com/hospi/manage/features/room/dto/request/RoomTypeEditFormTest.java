package com.hospi.manage.features.room.dto.request;

import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoomTypeEditFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        var form = new RoomTypeEditForm(
                RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "features",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenCategoryNull() {
        var form = new RoomTypeEditForm(null, RoomTier.SUPERIOR, 2, "desc", "features",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("category", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenTierNull() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, null, 2, "desc", "features",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("tier", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenMaxOccupancyNull() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, null, "desc", "features",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("maxOccupancy", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenMaxOccupancyBelowOne() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 0, "desc", "features",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("maxOccupancy", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenMaxOccupancyAboveMax() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 21, "desc", "features",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("maxOccupancy", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenDescriptionBlank() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "", "features",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenFeaturesBlank() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "",
                BedType.QUEEN, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("features", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenBedTypeNull() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "features",
                null, 30, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("bedType", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenAreaNull() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "features",
                BedType.QUEEN, null, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("area", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenAreaBelowOne() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "features",
                BedType.QUEEN, 0, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("area", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenAreaAboveMax() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "features",
                BedType.QUEEN, 201, BigDecimal.valueOf(150),
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("area", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenBasePriceNull() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "features",
                BedType.QUEEN, 30, null,
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("basePrice", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenBasePriceZero() {
        var form = new RoomTypeEditForm(RoomCategory.DOUBLE, RoomTier.SUPERIOR, 2, "desc", "features",
                BedType.QUEEN, 30, BigDecimal.ZERO,
                null, null, null);
        Set<ConstraintViolation<RoomTypeEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("basePrice", violations.iterator().next().getPropertyPath().toString());
    }
}
