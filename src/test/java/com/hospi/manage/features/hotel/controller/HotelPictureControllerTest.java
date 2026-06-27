package com.hospi.manage.features.hotel.controller;

import com.hospi.manage.features.hotel.entity.HotelPicture;
import com.hospi.manage.features.hotel.service.HotelPictureService;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelPictureController.class)
@AutoConfigureMockMvc(addFilters = false)
class HotelPictureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelPictureService hotelPictureService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getImage_shouldReturnImage() throws Exception {
        byte[] imageData = "test-image".getBytes();
        HotelPicture picture = new HotelPicture();
        picture.setImageData(imageData);
        when(hotelPictureService.findById(1L)).thenReturn(picture);

        mockMvc.perform(get("/hotel-picture/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(content().bytes(imageData));
    }
}
