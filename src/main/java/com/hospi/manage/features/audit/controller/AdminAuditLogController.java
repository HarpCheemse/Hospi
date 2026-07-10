package com.hospi.manage.features.audit.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for the admin audit log listing page. */
@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

    private static final int PAGE_SIZE = 25;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "AUDIT_LOGS");
    }

    @GetMapping
    String list(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        var paged = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("logs", paged);
        return "admin/audit-log/list";
    }
}
