package com.hospi.manage.features.notification.controller;

import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.notification.dto.NotificationDTO;
import com.hospi.manage.features.notification.dto.UnreadCountResponse;
import com.hospi.manage.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread-count")
    @ResponseBody
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal AccountPrincipal principal) {
        return notificationService.getUnreadCount(principal.getAccount().getId());
    }

    @GetMapping
    public String list(@AuthenticationPrincipal AccountPrincipal principal,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationDTO> notifications = notificationService.getNotifications(principal.getAccount().getId(), pageable);
        model.addAttribute("notifications", notifications);
        return "fragments/notification-list :: list";
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal AccountPrincipal principal) {
        notificationService.markAllRead(principal.getAccount().getId());
        return ResponseEntity.noContent().build();
    }
}