package com.hospi.manage.features.hotel.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.service.ImageCompressionService;
import com.hospi.manage.features.hotel.dto.HotelForm;
import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.hotel.entity.HotelPicture;
import com.hospi.manage.features.hotel.enums.HotelStatus;
import com.hospi.manage.features.hotel.repository.HotelPictureRepository;
import com.hospi.manage.features.hotel.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelPictureRepository hotelPictureRepository;

    @Mock
    private ImageCompressionService imageCompressionService;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void find_shouldReturnHotel_whenFound() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));

        Hotel result = hotelService.find();

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void find_shouldThrow_whenNotFound() {
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.find());
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

        HotelForm form = hotelService.getForm();

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

        hotelService.update(form, null, null, null);

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
    void update_shouldReplaceCoverImage_whenProvided() throws IOException {
        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>());
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenReturn(hotel);

        MultipartFile coverImage = mock(MultipartFile.class);
        when(coverImage.isEmpty()).thenReturn(false);

        byte[] compressedData = new byte[]{1, 2, 3, 4};
        when(imageCompressionService.toWebp(coverImage, 720, 0.75f)).thenReturn(compressedData);

        hotelService.update(new HotelForm(), coverImage, null, null);

        verify(hotelRepository).save(hotel);
        assertEquals(1, hotel.getPictures().size());
        assertEquals(0, hotel.getPictures().get(0).getSortOrder());
        assertArrayEquals(compressedData, hotel.getPictures().get(0).getImageData());
    }

    @Test
    void update_shouldThrow_whenHotelNotFound() {
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.update(new HotelForm(), null, null, null));
    }

    @Test
    void update_shouldReplaceExistingCoverImage_whenCoverExists() throws IOException {
        HotelPicture existingCover = new HotelPicture();
        existingCover.setId(1L);
        existingCover.setSortOrder(0);
        existingCover.setImageData(new byte[]{0});

        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>(List.of(existingCover)));
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenReturn(hotel);

        MultipartFile coverImage = mock(MultipartFile.class);
        when(coverImage.isEmpty()).thenReturn(false);

        byte[] compressedData = new byte[]{5, 6, 7, 8};
        when(imageCompressionService.toWebp(coverImage, 720, 0.75f)).thenReturn(compressedData);

        hotelService.update(new HotelForm(), coverImage, null, null);

        verify(hotelRepository).save(hotel);
        assertEquals(1, hotel.getPictures().size());
        assertArrayEquals(compressedData, hotel.getPictures().get(0).getImageData());
    }

    @Test
    void update_shouldAddGalleryImages_whenProvided() throws IOException {
        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>());
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenReturn(hotel);

        MultipartFile newImage1 = mock(MultipartFile.class);
        when(newImage1.isEmpty()).thenReturn(false);
        MultipartFile newImage2 = mock(MultipartFile.class);
        when(newImage2.isEmpty()).thenReturn(false);

        byte[] data1 = new byte[]{1};
        byte[] data2 = new byte[]{2};
        when(imageCompressionService.toWebp(newImage1, 720, 0.75f)).thenReturn(data1);
        when(imageCompressionService.toWebp(newImage2, 720, 0.75f)).thenReturn(data2);

        hotelService.update(new HotelForm(), null, new MultipartFile[]{newImage1, newImage2}, null);

        verify(hotelRepository).save(hotel);
        assertEquals(2, hotel.getPictures().size());
        assertEquals(1, hotel.getPictures().get(0).getSortOrder());
        assertEquals(2, hotel.getPictures().get(1).getSortOrder());
    }

    @Test
    void update_shouldRemoveImages_whenRemoveImageIdsProvided() throws IOException {
        HotelPicture pic1 = new HotelPicture();
        pic1.setId(1L);
        pic1.setSortOrder(0);
        HotelPicture pic2 = new HotelPicture();
        pic2.setId(2L);
        pic2.setSortOrder(1);

        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>(List.of(pic1, pic2)));
        when(hotelRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(hotelPictureRepository.findAllById(List.of(2L))).thenReturn(List.of(pic2));
        when(hotelRepository.save(any())).thenReturn(hotel);

        hotelService.update(new HotelForm(), null, null, List.of(2L));

        verify(hotelPictureRepository).deleteAll(List.of(pic2));
        verify(hotelRepository).save(hotel);
        assertEquals(1, hotel.getPictures().size());
    }
}
