package com.hospi.manage.features.manager.detail.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.utils.ImageUtils;
import com.hospi.manage.features.manager.detail.dto.HotelForm;
import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.entity.HotelPicture;
import com.hospi.manage.features.manager.detail.enums.HotelStatus;
import com.hospi.manage.features.manager.detail.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Test
    void find_shouldReturnHotel_whenFound() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));

        HotelService service = new HotelService(hotelRepository);
        Hotel result = service.find();

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void find_shouldThrow_whenNotFound() {
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.empty());

        HotelService service = new HotelService(hotelRepository);

        assertThrows(ResourceNotFoundException.class, service::find);
    }

    @Test
    void getForm_shouldReturnPopulatedForm() {
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setDescription("A nice hotel");
        hotel.setAddress("123 Main St");
        hotel.setPhone("555-0100");
        hotel.setEmail("test@hotel.com");
        hotel.setCheckInTime(LocalTime.of(14, 0));
        hotel.setCheckOutTime(LocalTime.of(11, 0));
        hotel.setFeatures("WiFi, Pool");
        hotel.setStatus(HotelStatus.ACTIVE);
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));

        HotelService service = new HotelService(hotelRepository);
        HotelForm form = service.getForm();

        assertEquals("Test Hotel", form.getName());
        assertEquals("A nice hotel", form.getDescription());
        assertEquals("123 Main St", form.getAddress());
        assertEquals("555-0100", form.getPhone());
        assertEquals("test@hotel.com", form.getEmail());
        assertEquals(LocalTime.of(14, 0), form.getCheckInTime());
        assertEquals(LocalTime.of(11, 0), form.getCheckOutTime());
        assertEquals("WiFi, Pool", form.getFeatures());
        assertEquals(HotelStatus.ACTIVE, form.getStatus());
    }

    @Test
    void update_shouldSetFields_andSave() throws IOException {
        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>());
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenReturn(hotel);

        HotelForm form = new HotelForm();
        form.setName("Updated Hotel");
        form.setDescription("Updated description");
        form.setAddress("456 Oak Ave");
        form.setPhone("555-0200");
        form.setEmail("updated@hotel.com");
        form.setCheckInTime(LocalTime.of(15, 0));
        form.setCheckOutTime(LocalTime.of(12, 0));
        form.setFeatures("Updated features");
        form.setStatus(HotelStatus.CLOSED);

        HotelService service = new HotelService(hotelRepository);
        service.update(form, null);

        verify(hotelRepository).save(hotel);
        assertEquals("Updated Hotel", hotel.getName());
        assertEquals("Updated description", hotel.getDescription());
        assertEquals("456 Oak Ave", hotel.getAddress());
        assertEquals("555-0200", hotel.getPhone());
        assertEquals("updated@hotel.com", hotel.getEmail());
        assertEquals(LocalTime.of(15, 0), hotel.getCheckInTime());
        assertEquals(LocalTime.of(12, 0), hotel.getCheckOutTime());
        assertEquals("Updated features", hotel.getFeatures());
        assertEquals(HotelStatus.CLOSED, hotel.getStatus());
    }

    @Test
    void update_shouldProcessImage_whenProvided() throws IOException {
        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>());
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenReturn(hotel);

        MultipartFile images = mock(MultipartFile.class);
        when(images.isEmpty()).thenReturn(false);

        byte[] compressedData = new byte[]{1, 2, 3, 4};

        HotelService service = new HotelService(hotelRepository);

        try (MockedStatic<ImageUtils> mocked = mockStatic(ImageUtils.class)) {
            mocked.when(() -> ImageUtils.compressWebP(images, 720, 0.75f)).thenReturn(compressedData);

            service.update(new HotelForm(), images);

            verify(hotelRepository).save(hotel);
            assertEquals(1, hotel.getPictures().size());
            assertArrayEquals(compressedData, hotel.getPictures().get(0).getImageData());
            mocked.verify(() -> ImageUtils.compressWebP(images, 720, 0.75f));
        }
    }

    @Test
    void update_shouldThrow_whenHotelNotFound() {
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.empty());

        HotelService service = new HotelService(hotelRepository);

        assertThrows(ResourceNotFoundException.class, () -> service.update(new HotelForm(), null));
    }
}
