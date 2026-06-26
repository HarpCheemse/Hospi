package com.hospi.manage.features.room.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.room.dto.response.RoomTypeView;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import com.hospi.manage.features.room.service.RoomTypeService;
import com.hospi.manage.features.room.validation.CreateRoomTypeValidator;
import com.hospi.manage.features.room.validation.EditRoomTypeValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomTypeService roomTypeService;

    @MockitoBean
    private CreateRoomTypeValidator createRoomTypeValidator;

    @MockitoBean
    private EditRoomTypeValidator editRoomTypeValidator;

    @MockitoBean
    private NotificationService notificationService;

    private RoomType aRoomType() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("SUPERIOR FAMILY");
        roomType.setCategory(RoomCategory.FAMILY);
        roomType.setTier(RoomTier.SUPERIOR);
        roomType.setBedType(BedType.QUEEN);
        roomType.setMaxOccupancy(2);
        roomType.setArea(30);
        roomType.setBasePrice(BigDecimal.valueOf(150));
        roomType.setDescription("Nice room");
        roomType.setFeatures("wifi");
        roomType.setActive(true);
        return roomType;
    }

    @Test
    void list_shouldRender_whenRoomTypesExist() throws Exception {
        when(roomTypeService.findAllViews()).thenReturn(List.of());

        mockMvc.perform(get("/manager/room-types"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room-type/list"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void createForm_shouldRender_whenRequested() throws Exception {
        mockMvc.perform(get("/manager/room-types/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room-type/create"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void detail_shouldRender_whenRoomTypeExists() throws Exception {
        when(roomTypeService.findViewById(1L)).thenReturn(RoomTypeView.from(aRoomType()));

        mockMvc.perform(get("/manager/room-types/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room-type/detail"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void edit_shouldRender_whenRoomTypeExists() throws Exception {
        when(roomTypeService.findById(1L)).thenReturn(aRoomType());

        mockMvc.perform(get("/manager/room-types/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room-type/edit"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void delete_shouldRedirect_whenRoomTypeExists() throws Exception {
        mockMvc.perform(post("/manager/room-types/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/room-types"));
    }

    @Test
    void delete_shouldRedirectToEdit_whenRoomTypeHasActiveRooms() throws Exception {
        doThrow(new IllegalStateException("Cannot delete room type with 2 active room(s) assigned"))
                .when(roomTypeService).delete(1L);

        mockMvc.perform(post("/manager/room-types/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/room-types/1/edit"))
                .andExpect(flash().attributeExists("error"));
    }
}
