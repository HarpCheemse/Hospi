package com.hospi.manage.features.room.controller;

import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import com.hospi.manage.features.room.service.RoomService;
import com.hospi.manage.features.room.service.RoomTypeService;
import com.hospi.manage.features.room.validation.RoomValidator;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private RoomTypeService roomTypeService;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private RoomValidator roomValidator;

    @MockitoBean
    private NotificationService notificationService;

    private Hotel aHotel() {
        Hotel hotel = new Hotel();
        hotel.setFloorCount((short) 5);
        return hotel;
    }

    private Room aRoom() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("BASIC SINGLE");
        roomType.setCategory(RoomCategory.SINGLE);
        roomType.setTier(RoomTier.BASIC);
        roomType.setBedType(BedType.SINGLE);
        roomType.setMaxOccupancy(1);
        roomType.setArea(25);
        roomType.setBasePrice(BigDecimal.valueOf(100));
        roomType.setDescription("Basic room");
        roomType.setFeatures("wifi");
        roomType.setActive(true);

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setRoomType(roomType);
        room.setFloorNumber((short) 1);
        room.setConditionStatus(ConditionStatus.CLEAN);
        return room;
    }

    @Test
    void list_shouldRender_whenCalled() throws Exception {
        Hotel hotel = aHotel();
        when(hotelService.find()).thenReturn(hotel);
        when(roomService.getFloorViews(null)).thenReturn(List.of());

        mockMvc.perform(get("/manager/rooms").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room/list"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(model().attributeExists("floors"));
    }

    @Test
    void list_shouldRender_withFloorFilter() throws Exception {
        Hotel hotel = aHotel();
        when(hotelService.find()).thenReturn(hotel);
        when(roomService.getFloorViews(1)).thenReturn(List.of());

        mockMvc.perform(get("/manager/rooms?floor=1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room/list"))
                .andExpect(model().attribute("selectedFloor", 1));
    }

    @Test
    void createForm_shouldRender_whenCalled() throws Exception {
        when(hotelService.find()).thenReturn(aHotel());
        when(roomTypeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/manager/rooms/create").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room/create"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(model().attributeExists("roomTypes"));
    }

    @Test
    void edit_shouldRender_whenRoomExists() throws Exception {
        Room room = aRoom();
        when(roomService.findById(1L)).thenReturn(room);
        when(roomTypeService.findAll()).thenReturn(List.of(room.getRoomType()));

        mockMvc.perform(get("/manager/rooms/1/edit").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room/edit"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("roomId", 1L))
                .andExpect(model().attributeExists("conditionStatuses"))
                .andExpect(model().attributeExists("roomTypes"));
    }

    @Test
    void editRoom_shouldRedirect_whenValid() throws Exception {
        Room room = aRoom();
        when(roomService.findById(1L)).thenReturn(room);
        when(roomTypeService.findAll()).thenReturn(List.of(room.getRoomType()));

        mockMvc.perform(post("/manager/rooms/1/edit").with(csrf())
                        .param("roomNumber", "102")
                        .param("roomTypeId", "1")
                        .param("conditionStatus", "CLEAN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/rooms"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void editRoom_shouldReRender_whenValidationFails() throws Exception {
        Room room = aRoom();
        when(roomService.findById(1L)).thenReturn(room);
        when(roomTypeService.findAll()).thenReturn(List.of(room.getRoomType()));

        mockMvc.perform(post("/manager/rooms/1/edit").with(csrf())
                        .param("roomNumber", "")
                        .param("roomTypeId", "")
                        .param("conditionStatus", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/room/edit"))
                .andExpect(model().attributeExists("roomTypes"))
                .andExpect(model().attributeExists("floor"));
    }

    @Test
    void delete_shouldRedirect_whenRoomExists() throws Exception {
        mockMvc.perform(post("/manager/rooms/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/rooms"));
    }

    @Test
    void delete_shouldRedirectToEdit_whenRoomHasActiveReservations() throws Exception {
        doThrow(new IllegalStateException("Cannot delete room 101 — it is referenced by active reservation(s)"))
                .when(roomService).delete(1L);

        mockMvc.perform(post("/manager/rooms/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/rooms/1/edit"))
                .andExpect(flash().attributeExists("error"));
    }
}
