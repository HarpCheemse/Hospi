package com.hospi.manage.features.room.controller;

import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.room.dto.response.RoomOccupancyView;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceptionistRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceptionistRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private NotificationService notificationService;

    private Hotel aHotel() {
        Hotel hotel = new Hotel();
        hotel.setFloorCount((short) 5);
        return hotel;
    }

    @Test
    void roomStatus_shouldRender_whenCalled() throws Exception {
        when(hotelService.find()).thenReturn(aHotel());
        when(roomService.getFloorViews(null)).thenReturn(List.of());

        mockMvc.perform(get("/receptionist/room-status").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/room-status"))
                .andExpect(model().attributeExists("hotel"))
                .andExpect(model().attributeExists("floors"))
                .andExpect(model().attributeDoesNotExist("selectedFloor"))
                .andExpect(content().string(containsString("Room Status")));
    }

    @Test
    void roomStatus_shouldRender_withFloorFilter() throws Exception {
        when(hotelService.find()).thenReturn(aHotel());
        when(roomService.getFloorViews(1)).thenReturn(List.of());

        mockMvc.perform(get("/receptionist/room-status?floor=1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/room-status"))
                .andExpect(model().attribute("selectedFloor", 1));
    }

    @Test
    void getOccupancy_shouldReturn_whenRoomOccupied() throws Exception {
        var occupancy = new RoomOccupancyView(1L, "101", "Deluxe", OccupancyStatus.OCCUPIED,
                ConditionStatus.CLEAN, null, null, "John", null, null, List.of());
        when(roomService.getRoomOccupancy(1L)).thenReturn(occupancy);

        mockMvc.perform(get("/receptionist/room-status/1/occupancy").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.occupancyStatus").value("OCCUPIED"))
                .andExpect(jsonPath("$.guestName").value("John"));
    }

    @Test
    void getOccupancy_shouldReturn_whenRoomVacant() throws Exception {
        var occupancy = new RoomOccupancyView(1L, "101", "Deluxe", OccupancyStatus.VACANT,
                ConditionStatus.CLEAN, null, null, null, null, null, List.of());
        when(roomService.getRoomOccupancy(1L)).thenReturn(occupancy);

        mockMvc.perform(get("/receptionist/room-status/1/occupancy").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupancyStatus").value("VACANT"))
                .andExpect(jsonPath("$.guestName").doesNotExist());
    }

    @Test
    void updateCondition_shouldReturn_whenValid() throws Exception {
        mockMvc.perform(post("/receptionist/room-status/1/condition")
                        .with(csrf())
                        .param("conditionStatus", "CLEAN"))
                .andExpect(status().isOk());

        verify(roomService).updateConditionStatus(1L, ConditionStatus.CLEAN);
    }
}
