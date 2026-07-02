package com.hospi.manage.features.hotel.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.hotel.entity.HotelPicture;
import com.hospi.manage.features.hotel.repository.HotelPictureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelPictureServiceTest {

    @Mock
    private HotelPictureRepository hotelPictureRepository;

    @Test
    void findById_shouldReturnPicture_whenFound() {
        HotelPicture picture = new HotelPicture();
        picture.setId(1L);
        when(hotelPictureRepository.findById(1L)).thenReturn(Optional.of(picture));

        HotelPictureService service = new HotelPictureService(hotelPictureRepository);
        HotelPicture result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(hotelPictureRepository.findById(1L)).thenReturn(Optional.empty());

        HotelPictureService service = new HotelPictureService(hotelPictureRepository);

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }
}
