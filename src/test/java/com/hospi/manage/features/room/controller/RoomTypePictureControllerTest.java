package com.hospi.manage.features.room.controller;

import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.room.entity.RoomTypePicture;
import com.hospi.manage.features.room.service.RoomTypePictureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomTypePictureController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomTypePictureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomTypePictureService roomTypePictureService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getImage_shouldReturnImage() throws Exception {
        byte[] imageData = "test-image".getBytes();
        RoomTypePicture picture = new RoomTypePicture();
        picture.setImageData(imageData);
        when(roomTypePictureService.findById(1L)).thenReturn(picture);

        mockMvc.perform(get("/room-type-picture/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(content().bytes(imageData));
    }
}
