package com.hospi.manage.features.manager.detail.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.manager.detail.dto.HotelForm;
import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.service.HotelService;
import com.hospi.manage.features.notification.service.NotificationService;
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

@WebMvcTest(HotelDetailController.class)
@AutoConfigureMockMvc(addFilters = false)
class HotelDetailControllerTest {

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
                .andExpect(view().name("manager/detail/detail"))
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
                .andExpect(view().name("manager/detail/edit"))
                .andExpect(model().attributeExists("hotelForm"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(content().string(containsString("Grand Hotel")));
    }

    @Test
    void updateDetail_shouldRedirectWithSuccessFlash() throws Exception {
        mockMvc.perform(post("/manager/details/edit")
                        .param("name", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/details"))
                .andExpect(flash().attribute(Attributes.SUCCESS, "Hotel details updated."));
    }

    @Test
    void updateDetail_shouldRedirectWithErrorFlash_whenIllegalState() throws Exception {
        doThrow(new IllegalStateException("Update failed"))
                .when(hotelService).update(any(), any(), any(), any());

        mockMvc.perform(post("/manager/details/edit")
                        .param("name", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(Attributes.ERROR, "Update failed"));
    }
}
