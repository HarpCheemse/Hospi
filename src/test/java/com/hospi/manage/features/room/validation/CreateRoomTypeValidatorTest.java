package com.hospi.manage.features.room.validation;

import com.hospi.manage.features.room.dto.request.RoomTypeCreateForm;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRoomTypeValidatorTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private CreateRoomTypeValidator validator;

    private RoomTypeCreateForm form;
    private Errors errors;

    @BeforeEach
    void setup() {
        form = mock(RoomTypeCreateForm.class);
        errors = new BeanPropertyBindingResult(form, "form");
    }

    @Test
    void shouldPass_whenValidForm() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));

        MockMultipartFile file = new MockMultipartFile(
                "coverImage",
                "test.jpg",
                "image/jpeg",
                "data".getBytes()
        );
        when(form.coverImage()).thenReturn(file);

        when(roomTypeRepository.existsByCategoryAndTierAndActiveTrue(any(),
                any()))
                .thenReturn(false);

        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void shouldFail_whenDuplicateRoomTypeExists() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));
        when(form.coverImage()).thenReturn(null);

        when(roomTypeRepository.existsByCategoryAndTierAndActiveTrue(any(),
                any()))
                .thenReturn(true);

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("tier"));
    }

    @Test
    void shouldFail_whenCoverImageTooLarge() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));

        MockMultipartFile file = new MockMultipartFile(
                "coverImage",
                "test.jpg",
                "image/jpeg",
                new byte[6 * 1024 * 1024]
        );
        when(form.coverImage()).thenReturn(file);

        when(roomTypeRepository.existsByCategoryAndTierAndActiveTrue(any(),
                any()))
                .thenReturn(false);

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("coverImage"));
    }

    @Test
    void shouldFail_whenInvalidImageType() {
        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.basePrice()).thenReturn(new BigDecimal("100.00"));

        MockMultipartFile file = new MockMultipartFile(
                "coverImage",
                "test.gif",
                "image/gif",
                "data".getBytes()
        );
        when(form.coverImage()).thenReturn(file);

        when(roomTypeRepository.existsByCategoryAndTierAndActiveTrue(any(),
                any()))
                .thenReturn(false);

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("coverImage"));
    }
}
