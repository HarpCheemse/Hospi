package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.room.dto.response.RoomTypeView;
import com.hospi.manage.features.room.service.RoomTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomTypeService roomTypeService;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void rooms_shouldRender() throws Exception {
        when(roomTypeService.findAllViews()).thenReturn(List.of());

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/rooms"))
                .andExpect(model().attributeExists(ROOM_TYPES))
                .andExpect(content().string(containsString("Rooms")));
    }
}
