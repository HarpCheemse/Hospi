package com.hospi.manage.features.notification.service;

import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.notification.dto.NotificationDTO;
import com.hospi.manage.features.notification.entity.Notification;
import com.hospi.manage.features.notification.repository.NotificationRepository;
import com.hospi.manage.features.notification.repository.StaffNotificationViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StaffNotificationViewRepository staffNotificationViewRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getUnreadCount_shouldReturnCount() {
        when(notificationRepository.countUnreadByStaffId(1L)).thenReturn(5);

        int count = notificationService.getUnreadCount(1L);

        assertEquals(5, count);
        verify(notificationRepository).countUnreadByStaffId(1L);
    }

    @Test
    void getUnreadCount_shouldReturnZero_whenNoUnread() {
        when(notificationRepository.countUnreadByStaffId(1L)).thenReturn(0);

        int count = notificationService.getUnreadCount(1L);

        assertEquals(0, count);
    }

    @Test
    void getNotifications_shouldReturnPage() {
        var notificationPage = mock(Page.class);
        when(notificationRepository.findAllByStaffId(1L, Pageable.unpaged())).thenReturn(notificationPage);
        when(notificationPage.map(any())).thenReturn(mock(Page.class));

        Page<NotificationDTO> result = notificationService.getNotifications(1L, Pageable.unpaged());

        assertNotNull(result);
        verify(notificationRepository).findAllByStaffId(1L, Pageable.unpaged());
    }

    @Test
    void markAllRead_shouldUpsertLastViewedAt() {
        notificationService.markAllRead(1L);

        verify(staffNotificationViewRepository).upsertLastViewedAt(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void notifyRole_shouldSaveNotification() {
        Notification notification = Notification.builder().id(1L).build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.notifyRole(Role.RECEPTIONIST, "Title", "Body");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notifyRole_shouldSetAllFields() {
        Notification notification = Notification.builder().id(1L).build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.notifyRole(Role.RECEPTIONIST, "Title", "Body");

        verify(notificationRepository).save(argThat(n ->
                n.getRole() == Role.RECEPTIONIST
                        && "Title".equals(n.getTitle())
                        && "Body".equals(n.getBody())
                        && n.getCreatedAt() != null
        ));
    }
}
