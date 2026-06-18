package com.hospi.manage.features.room.room_type;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.room.dto.room_type.RoomTypeCreateForm;
import com.hospi.manage.features.room.dto.room_type.RoomTypeEditForm;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import com.hospi.manage.features.room.service.RoomTypePictureService;
import com.hospi.manage.features.room.service.RoomTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RoomTypeServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RoomTypePictureService roomTypePictureService;

    @InjectMocks
    private RoomTypeService service;

    @Test
    void createRoomType_shouldSaveRoomType() throws Exception {
        RoomTypeCreateForm form = mock(RoomTypeCreateForm.class);

        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.maxOccupancy()).thenReturn(2);
        when(form.description()).thenReturn("desc");
        when(form.features()).thenReturn("wifi");
        when(form.bedType()).thenReturn(null);
        when(form.area()).thenReturn(30);
        when(form.basePrice()).thenReturn(BigDecimal.valueOf(100));
        when(form.coverImage()).thenReturn(null);

        RoomType saved = new RoomType();
        saved.setId(1L);

        when(roomTypeRepository.save(any(RoomType.class)))
                .thenReturn(saved);

        RoomType result = service.createRoomType(form);

        assertEquals(1L,
                result.getId());

        verify(roomTypePictureService).replaceCover(any(RoomType.class),
                eq(null));
        verify(roomTypeRepository).save(any(RoomType.class));
    }

    @Test
    void findById_shouldReturnRoomType() {
        RoomType rt = new RoomType();
        rt.setId(1L);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(rt));

        RoomType result = service.findById(1L);

        assertEquals(1L,
                result.getId());
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    void update_shouldCallAllPictureOperations() throws Exception {
        RoomType rt = new RoomType();
        rt.setPictures(new ArrayList<>());

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(rt));

        RoomTypeEditForm form = mock(RoomTypeEditForm.class);

        when(form.category()).thenReturn(RoomCategory.FAMILY);
        when(form.tier()).thenReturn(RoomTier.SUPERIOR);
        when(form.bedType()).thenReturn(null);
        when(form.maxOccupancy()).thenReturn(2);
        when(form.area()).thenReturn(30);
        when(form.basePrice()).thenReturn(BigDecimal.valueOf(100));
        when(form.description()).thenReturn("desc");
        when(form.features()).thenReturn("wifi");
        when(form.coverImage()).thenReturn(null);
        when(form.newImages()).thenReturn(null);
        when(form.removeImageIds()).thenReturn(null);

        service.update(1L,
                form);

        verify(roomTypePictureService).replaceCover(rt,
                null);
        verify(roomTypePictureService).addNewImages(rt,
                null);
        verify(roomTypePictureService).removeImages(rt,
                null);
        verify(roomTypeRepository).save(rt);
    }

    @Test
    void delete_shouldSoftDelete() {
        RoomType rt = new RoomType();
        rt.setActive(true);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(rt));

        service.delete(1L);

        assertFalse(rt.getActive());
        verify(roomTypeRepository).save(rt);
    }

    @Test
    void findAllViews_shouldMap() {
        RoomType rt = new RoomType();
        rt.setId(1L);

        when(roomTypeRepository.findAll())
                .thenReturn(List.of(rt));

        var result = service.findAllViews();

        assertEquals(1,
                result.size());
    }
}