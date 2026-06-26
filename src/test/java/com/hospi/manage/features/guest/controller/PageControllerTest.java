package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.entity.HotelPicture;
import com.hospi.manage.features.manager.detail.service.HotelService;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
@AutoConfigureMockMvc(addFilters = false)
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void home_shouldRender() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Grand Hotel");
        hotel.setPictures(List.of(new HotelPicture()));
        when(hotelService.find()).thenReturn(hotel);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/home"))
                .andExpect(model().attributeExists(HOTEL, BANNERS))
                .andExpect(content().string(containsString("Grand Hotel")));
    }

    @Test
    void policies_shouldRender() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Grand Hotel");
        hotel.setPictures(List.of(new HotelPicture()));
        when(hotelService.find()).thenReturn(hotel);

        mockMvc.perform(get("/policies"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/policies"))
                .andExpect(content().string(containsString("Grand Hotel")));
    }

    @Test
    void contact_shouldRender() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Grand Hotel");
        hotel.setPictures(List.of(new HotelPicture()));
        when(hotelService.find()).thenReturn(hotel);

        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/contact"))
                .andExpect(content().string(containsString("Grand Hotel")));
    }

    @Test
    void home_shouldHandleDataAccessException() throws Exception {
        when(hotelService.find()).thenThrow(new DataAccessException("DB down") {});

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/home"))
                .andExpect(model().attribute(HOTEL, (Object) null))
                .andExpect(model().attribute(BANNERS, (Object) null));
    }
}
