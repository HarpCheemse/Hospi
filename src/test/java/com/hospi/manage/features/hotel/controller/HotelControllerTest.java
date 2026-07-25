package com.hospi.manage.features.hotel.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.hotel.dto.HotelForm;
import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.notification.service.NotificationService;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(HotelController.class)
@AutoConfigureMockMvc(addFilters = false)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void detail_shouldRender() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Grand Hotel");
        when(hotelService.find()).thenReturn(hotel);

        mockMvc.perform(get("/manager/details"))
                .andExpect(status().isOk())
                .andExpect(view().name("hotel/detail"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(content().string(containsString("Grand Hotel")));
    }

    @Test
    void edit_shouldRender() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Grand Hotel");
        HotelForm form = new HotelForm();
        form.setName("Grand Hotel");
        when(hotelService.find()).thenReturn(hotel);
        when(hotelService.getForm()).thenReturn(form);

        mockMvc.perform(get("/manager/details/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("hotel/edit"))
                .andExpect(model().attributeExists("hotelForm"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(content().string(containsString("Grand Hotel")));
    }

    @Test
    void updateDetail_shouldReRender_whenValidationFails() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>());
        when(hotelService.find()).thenReturn(hotel);

        mockMvc.perform(post("/manager/details/edit")
                        .param("name", "")
                        .param("address", "")
                        .param("phone", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("hotel/edit"))
                .andExpect(model().attributeHasFieldErrors("hotelForm", "name"));
    }

    @Test
    void updateDetail_shouldRedirectWithSuccessFlash() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>());
        when(hotelService.find()).thenReturn(hotel);

        mockMvc.perform(post("/manager/details/edit")
                        .param("name", "Updated Hotel")
                        .param("address", "123 Main St")
                        .param("phone", "+1234567890")
                        .param("checkInTime", "14:00")
                        .param("checkOutTime", "11:00")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/details"))
                .andExpect(flash().attribute(Attributes.SUCCESS, "Hotel details updated."));
    }

    @Test
    void updateDetail_shouldRedirectWithErrorFlash_whenIllegalState() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setPictures(new ArrayList<>());
        when(hotelService.find()).thenReturn(hotel);

        doThrow(new IllegalStateException("Update failed"))
                .when(hotelService).update(any(), any(), any(), any());

        mockMvc.perform(post("/manager/details/edit")
                        .param("name", "Updated Hotel")
                        .param("address", "123 Main St")
                        .param("phone", "+1234567890")
                        .param("checkInTime", "14:00")
                        .param("checkOutTime", "11:00")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(Attributes.ERROR, "Update failed"));
    }
}
