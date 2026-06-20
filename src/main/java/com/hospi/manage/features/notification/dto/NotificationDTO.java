package com.hospi.manage.features.notification.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;
    private String title;
    private String body;
    private String relatedModule;
    private String relatedId;
    private LocalDateTime createdAt;
}