package com.hospi.manage.features.leader.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
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

    // Mock security-related beans needed for the application context to load during this @WebMvcTest
    @MockitoBean
    private com.hospi.manage.core.security.session.AccountUserDetailsService accountUserDetailsService;

    @MockitoBean
    private com.hospi.manage.config.CustomAuthEntryPoint customAuthEntryPoint;

    @MockitoBean
    private com.hospi.manage.core.security.session.RoleBasedAuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler;

    @Test
    @WithMockUser(roles = "LEADER")
    void dashboard_shouldRenderLeaderDashboardAndPassDirtyRooms() throws Exception {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setRoomNumber("101");
        room1.setConditionStatus(ConditionStatus.DIRTY);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setRoomNumber("102");
        room2.setConditionStatus(ConditionStatus.DIRTY);

        when(roomService.getActiveDirtyRooms()).thenReturn(List.of(room1, room2));

        mockMvc.perform(get("/leader"))
                .andExpect(status().isOk())
                .andExpect(view().name("leader/dashboard"))
                .andExpect(model().attributeExists("dirtyRooms"))
                .andExpect(model().attribute("dirtyRooms", List.of(room1, room2)))
                .andExpect(model().attribute(Attributes.ACTIVE_SIDEBAR, "TASKS"));

        verify(roomService, times(1)).getActiveDirtyRooms();
    }

    @Test
    @WithMockUser(roles = "LEADER")
    void markClean_shouldCallServiceAndReturnOk() throws Exception {
        mockMvc.perform(post("/leader/rooms/1/clean").with(csrf()))
                .andExpect(status().isOk());

        verify(roomService, times(1)).updateConditionStatus(1L, ConditionStatus.CLEAN);
    }
}
