package com.hospi.manage.features.notification.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.notification.dto.NotificationDTO;
import com.hospi.manage.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationPageController {

    private final NotificationService notificationService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "NOTIFICATIONS");
    }

    private String notificationViewPath(Role role) {
        return switch (role) {
            case ADMIN -> "admin/notifications";
            case MANAGER -> "manager/notifications";
            case RECEPTIONIST -> "receptionist/notifications";
            case LEADER -> "leader/notifications";
        };
    }

    @GetMapping
    String notifications(@AuthenticationPrincipal AccountPrincipal principal,
                         @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model) {
        Page<NotificationDTO> notifications = notificationService.getNotifications(
                principal.getAccount().getId(), pageable);
        model.addAttribute("notifications", notifications);
        return notificationViewPath(principal.getAccount().getRole());
    }

    @PostMapping("/mark-all-read")
    public String markAllRead(@AuthenticationPrincipal AccountPrincipal principal,
                              RedirectAttributes redirectAttributes) {
        notificationService.markAllRead(principal.getAccount().getId());
        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
