package com.hospi.manage.common.advice;

import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final NotificationService notificationService;

    @ModelAttribute("unreadNotificationCount")
    public int unreadNotificationCount(@AuthenticationPrincipal AccountPrincipal principal) {
        if (principal == null) {
            return 0;
        }
        return notificationService.getUnreadCount(principal.getAccount().getId());
    }
}
