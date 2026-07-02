package com.hospi.manage.features.notification.service;

import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.notification.dto.NotificationDTO;
import com.hospi.manage.features.notification.entity.Notification;
import com.hospi.manage.features.notification.entity.StaffNotificationView;
import com.hospi.manage.features.notification.repository.NotificationRepository;
import com.hospi.manage.features.notification.repository.StaffNotificationViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StaffNotificationViewRepository staffNotificationViewRepository;

    public int getUnreadCount(Long staffId) {
        return notificationRepository.countUnreadByStaffId(staffId);
    }

    public Page<NotificationDTO> getNotifications(Long staffId, Pageable pageable) {
        return notificationRepository.findAllByStaffId(staffId, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public void markAllRead(Long staffId) {
        staffNotificationViewRepository.upsertLastViewedAt(staffId, LocalDateTime.now());
    }

    @Transactional
    public void notifyRole(Role role, String title, String body,
                           String relatedModule, String relatedId) {
        Notification notification = Notification.builder()
                .role(role)
                .title(title)
                .body(body)
                .relatedModule(relatedModule)
                .relatedId(relatedId)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .body(n.getBody())
                .relatedModule(n.getRelatedModule())
                .relatedId(n.getRelatedId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}