package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.room.dto.request.RoomTypeCreateForm;
import com.hospi.manage.features.room.dto.response.RoomTypeView;
import com.hospi.manage.features.room.dto.request.RoomTypeEditForm;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RoomRepository roomRepository;

    private RoomTypePictureService roomTypePictureService;
    private RoomTypeService service;

    @BeforeEach
    void setUp() {
        roomTypePictureService = mock(RoomTypePictureService.class);
        service = new RoomTypeService(roomTypeRepository, roomTypePictureService, roomRepository);
    }

    @Test
    void shouldSave_whenValidForm() throws Exception {
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

        assertEquals(1L, result.getId());

        verify(roomTypePictureService).replaceCover(any(RoomType.class),
                eq(null));
        verify(roomTypeRepository).save(any(RoomType.class));
    }

    @Test
    void createRoomType_shouldCallReplaceCoverAfterSave() throws Exception {
        RoomTypeCreateForm form = mock(RoomTypeCreateForm.class);
        when(form.category()).thenReturn(RoomCategory.DOUBLE);
        when(form.tier()).thenReturn(RoomTier.BASIC);
        when(form.maxOccupancy()).thenReturn(2);
        when(form.description()).thenReturn("desc");
        when(form.features()).thenReturn("wifi");
        when(form.bedType()).thenReturn(null);
        when(form.area()).thenReturn(25);
        when(form.basePrice()).thenReturn(BigDecimal.valueOf(80));
        when(form.coverImage()).thenReturn(null);

        RoomType saved = new RoomType();
        saved.setId(1L);
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(saved);

        service.createRoomType(form);

        var inOrder = inOrder(roomTypeRepository, roomTypePictureService);
        inOrder.verify(roomTypeRepository).save(any(RoomType.class));
        inOrder.verify(roomTypePictureService).replaceCover(any(RoomType.class),
                eq(null));
    }

    @Test
    void shouldReturn_whenActiveAndFound() {
        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setActive(true);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(rt));

        RoomType result = service.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrow_whenNotFound() {
        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    void shouldThrow_whenInactive() {
        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setActive(false);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(rt));

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    void shouldReturnActiveOnly_whenFindAll() {
        RoomType rt = new RoomType();
        rt.setId(1L);

        when(roomTypeRepository.findByActiveTrue())
                .thenReturn(List.of(rt));

        List<RoomType> result = service.findAll();

        assertEquals(1, result.size());
        verify(roomTypeRepository).findByActiveTrue();
    }

    @Test
    void shouldUpdate_whenValidForm() throws Exception {
        RoomType rt = new RoomType();
        rt.setPictures(new ArrayList<>());
        rt.setActive(true);

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

        service.update(1L, form);

        verify(roomTypePictureService).replaceCover(rt, null);
        verify(roomTypePictureService).addNewImages(rt, null);
        verify(roomTypePictureService).removeImages(rt, null);
        verify(roomTypeRepository).save(rt);
    }

    @Test
    void shouldSoftDelete_whenNoActiveRooms() {
        RoomType rt = new RoomType();
        rt.setActive(true);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(rt));
        when(roomRepository.countByRoomTypeIdAndActiveTrue(1L))
                .thenReturn(0L);

        service.delete(1L);

        assertFalse(rt.getActive());
        verify(roomTypeRepository).save(rt);
    }

    @Test
    void shouldThrow_whenActiveRoomsExist() {
        RoomType rt = new RoomType();
        rt.setActive(true);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(rt));
        when(roomRepository.countByRoomTypeIdAndActiveTrue(1L))
                .thenReturn(3L);

        assertThrows(IllegalStateException.class,
                () -> service.delete(1L));

        verify(roomTypeRepository, never()).save(any());
    }

    @Test
    void shouldReturnViews_whenFindAllWithRelations() {
        RoomType rt = new RoomType();
        rt.setId(1L);

        when(roomTypeRepository.findAllWithRelations())
                .thenReturn(List.of(rt));

        var result = service.findAllViews();

        assertEquals(1, result.size());
        verify(roomTypeRepository).findAllWithRelations();
    }

    @Test
    void shouldReturnView_whenFindViewById() {
        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setActive(true);
        rt.setName("SUPERIOR FAMILY");

        when(roomTypeRepository.findByIdWithRelations(1L)).thenReturn(rt);

        RoomTypeView result = service.findViewById(1L);

        assertEquals(1L, result.id());
    }

    @Test
    void findView_shouldReturnViewForActiveRoomType() {
        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setActive(true);
        when(roomTypeRepository.findByIdWithRelations(1L)).thenReturn(rt);

        var view = service.findView(1L);
        assertNotNull(view);
        assertEquals(1L, view.id());
    }

    @Test
    void findView_shouldThrowForInactiveRoomType() {
        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setActive(false);
        when(roomTypeRepository.findByIdWithRelations(1L)).thenReturn(rt);

        assertThrows(ResourceNotFoundException.class, () -> service.findView(1L));
    }

    @Test
    void findView_shouldThrowForMissingRoomType() {
        when(roomTypeRepository.findByIdWithRelations(99L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.findView(99L));
    }
}
