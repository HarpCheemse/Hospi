package com.hospi.manage.features.room.room_type;

import com.hospi.manage.features.room.dto.room_type.RoomTypeEditForm;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import com.hospi.manage.features.room.validation.EditRoomTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EditRoomTypeValidatorTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private EditRoomTypeValidator validator;

    private RoomTypeEditForm form;
    private Errors errors;

    @BeforeEach
    void setup() {
        form = mock(RoomTypeEditForm.class);
        errors = new BeanPropertyBindingResult(form,
                "form");
    }

    @Test
    void shouldPass_whenValidEdit() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.features()).thenReturn("wifi, tv");
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));

        MockMultipartFile cover = new MockMultipartFile("coverImage",
                "img.jpg",
                "image/jpeg",
                "data".getBytes());

        when(form.coverImage()).thenReturn(cover);
        when(form.newImages()).thenReturn(null);
        when(form.removeImageIds()).thenReturn(null);

        when(roomTypeRepository.existsByCategoryAndTierAndIdNot(any(),
                any(),
                anyLong())).thenReturn(false);

        when(roomTypeRepository.existsByCategoryAndTier(any(),
                any())).thenReturn(false);

        validator.validate(1L,
                form,
                0,
                errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void shouldFail_whenDuplicateCategoryTier() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.features()).thenReturn("wifi");
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));

        when(roomTypeRepository.existsByCategoryAndTierAndIdNot(any(),
                any(),
                anyLong())).thenReturn(true);

        validator.validate(1L,
                form,
                0,
                errors);

        assertTrue(errors.hasFieldErrors("tier"));
    }

    @Test
    void shouldFail_whenTooManyImages() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.features()).thenReturn("wifi");
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));

        List<MultipartFile> images = List.of(new MockMultipartFile("f1",
                        "a.jpg",
                        "image/jpeg",
                        "a".getBytes()),
                new MockMultipartFile("f2",
                        "b.jpg",
                        "image/jpeg",
                        "b".getBytes()),
                new MockMultipartFile("f3",
                        "c.jpg",
                        "image/jpeg",
                        "c".getBytes()),
                new MockMultipartFile("f4",
                        "d.jpg",
                        "image/jpeg",
                        "d".getBytes()),
                new MockMultipartFile("f5",
                        "e.jpg",
                        "image/jpeg",
                        "e".getBytes()),
                new MockMultipartFile("f6",
                        "f.jpg",
                        "image/jpeg",
                        "f".getBytes()));

        when(form.newImages()).thenReturn(images);

        validator.validate(1L,
                form,
                0,
                errors);

        assertTrue(errors.hasFieldErrors("newImages"));
    }

    @Test
    void shouldFail_whenInvalidFeatureFormat() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));
        when(form.newImages()).thenReturn(null);

        when(form.features()).thenReturn("wifi, @@bad");

        validator.validate(1L,
                form,
                0,
                errors);

        assertTrue(errors.hasFieldErrors("features"));
    }

    @Test
    void shouldFail_whenDuplicateFeatures() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));
        when(form.newImages()).thenReturn(null);

        when(form.features()).thenReturn("wifi, wifi");

        validator.validate(1L,
                form,
                0,
                errors);

        assertTrue(errors.hasFieldErrors("features"));
    }

    @Test
    void shouldFail_whenPriceHasTooManyDecimals() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.features()).thenReturn("wifi");

        when(form.basePrice()).thenReturn(new BigDecimal("10.123"));

        validator.validate(1L,
                form,
                0,
                errors);

        assertTrue(errors.hasFieldErrors("basePrice"));
    }
}