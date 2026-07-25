package com.hospi.manage.features.audit.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.audit.enums.AuditAction;
import com.hospi.manage.features.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/** Controller for the admin audit log listing page. */
@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

    private static final int PAGE_SIZE = 10;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "AUDIT_LOGS");
    }

    @GetMapping
    String list(@RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(required = false) String action,
                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                Model model) {
        AuditAction actionEnum = null;
        if (action != null && !action.isBlank()) {
            try {
                actionEnum = AuditAction.valueOf(action);
            } catch (IllegalArgumentException e) {
                actionEnum = null;
            }
        }
        var paged = auditLogRepository.findFiltered(actionEnum, startDate, endDate,
                PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("logs", paged);
        model.addAttribute("filterAction", action);
        model.addAttribute("filterStart", startDate);
        model.addAttribute("filterEnd", endDate);
        model.addAttribute("allActions", AuditAction.values());
        return "admin/audit-log/list";
    }
}
