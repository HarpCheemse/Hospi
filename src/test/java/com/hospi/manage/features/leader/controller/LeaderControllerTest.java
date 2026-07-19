package com.hospi.manage.features.leader.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaderController.class)
class LeaderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private com.hospi.manage.features.notification.service.NotificationService notificationService;

    @MockitoBean
    private com.hospi.manage.core.security.session.AccountUserDetailsService accountUserDetailsService;

    @MockitoBean
    private com.hospi.manage.config.CustomAuthEntryPoint customAuthEntryPoint;

    @MockitoBean
    private com.hospi.manage.core.security.session.RoleBasedAuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler;

    @Test
    @WithMockUser(roles = "LEADER")
    void showDashboard_shouldRender() throws Exception {
        Room dirty1 = new Room();
        dirty1.setId(1L);
        dirty1.setRoomNumber("101");
        dirty1.setFloorNumber((short) 1);
        dirty1.setConditionStatus(ConditionStatus.DIRTY);
        dirty1.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        dirty1.setActive(true);

        Room dirty2 = new Room();
        dirty2.setId(2L);
        dirty2.setRoomNumber("102");
        dirty2.setFloorNumber((short) 1);
        dirty2.setConditionStatus(ConditionStatus.DIRTY);
        dirty2.setOccupancyStatus(OccupancyStatus.VACANT);
        dirty2.setActive(true);

        Room clean1 = new Room();
        clean1.setId(3L);
        clean1.setRoomNumber("103");
        clean1.setFloorNumber((short) 1);
        clean1.setConditionStatus(ConditionStatus.CLEAN);
        clean1.setOccupancyStatus(OccupancyStatus.VACANT);
        clean1.setActive(true);

        Room clean2 = new Room();
        clean2.setId(4L);
        clean2.setRoomNumber("104");
        clean2.setFloorNumber((short) 1);
        clean2.setConditionStatus(ConditionStatus.CLEAN);
        clean2.setOccupancyStatus(OccupancyStatus.VACANT);
        clean2.setActive(true);

        Room maintenance = new Room();
        maintenance.setId(5L);
        maintenance.setRoomNumber("105");
        maintenance.setFloorNumber((short) 1);
        maintenance.setConditionStatus(ConditionStatus.MAINTENANCE);
        maintenance.setOccupancyStatus(OccupancyStatus.VACANT);
        maintenance.setActive(true);

        when(roomService.findAll()).thenReturn(List.of(dirty1, dirty2, clean1, clean2, maintenance));

        mockMvc.perform(get("/leader"))
                .andExpect(status().isOk())
                .andExpect(view().name("leader/dashboard"))
                .andExpect(model().attributeExists(Attributes.VIEW))
                .andExpect(model().attribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Leader Dashboard")));

        verify(roomService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "LEADER")
    void showTasks_shouldRender() throws Exception {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setRoomNumber("101");
        room1.setFloorNumber((short) 1);
        room1.setConditionStatus(ConditionStatus.DIRTY);
        room1.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        room1.setActive(true);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setRoomNumber("201");
        room2.setFloorNumber((short) 2);
        room2.setConditionStatus(ConditionStatus.DIRTY);
        room2.setOccupancyStatus(OccupancyStatus.VACANT);
        room2.setActive(true);

        Room clean = new Room();
        clean.setId(3L);
        clean.setRoomNumber("103");
        clean.setFloorNumber((short) 1);
        clean.setConditionStatus(ConditionStatus.CLEAN);
        clean.setOccupancyStatus(OccupancyStatus.VACANT);
        clean.setActive(true);

        when(roomService.getActiveDirtyRooms()).thenReturn(List.of(room1, room2));
        when(roomService.findAll()).thenReturn(List.of(room1, room2, clean));

        mockMvc.perform(get("/leader/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("leader/tasks"))
                .andExpect(model().attributeExists("roomsByFloor"))
                .andExpect(model().attribute(Attributes.ACTIVE_SIDEBAR, "TASKS"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cleaning Tasks")));

        verify(roomService, times(1)).getActiveDirtyRooms();
        verify(roomService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "LEADER")
    void showTasks_shouldRenderEmpty_whenNoDirtyRooms() throws Exception {
        when(roomService.getActiveDirtyRooms()).thenReturn(List.of());
        when(roomService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/leader/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("leader/tasks"))
                .andExpect(model().attributeExists("roomsByFloor"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("All tasks completed")));

        verify(roomService, times(1)).getActiveDirtyRooms();
        verify(roomService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "LEADER")
    void markClean_shouldCallServiceAndReturnOk() throws Exception {
        mockMvc.perform(post("/leader/rooms/1/clean").with(csrf()))
                .andExpect(status().isOk());

        verify(roomService, times(1)).updateConditionStatus(1L, ConditionStatus.CLEAN);
    }
}
