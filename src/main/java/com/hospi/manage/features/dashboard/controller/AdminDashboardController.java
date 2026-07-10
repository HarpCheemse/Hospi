package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Controller for the admin dashboard landing page. */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AccountRepository accountRepository;
    private final AuditService auditService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD");
        model.addAttribute("totalStaff", accountRepository.count());
        model.addAttribute("adminCount", accountRepository.findByRole(Role.ADMIN).size());
        model.addAttribute("managerCount", accountRepository.findByRole(Role.MANAGER).size());
        model.addAttribute("receptionistCount", accountRepository.findByRole(Role.RECEPTIONIST).size());
        model.addAttribute("leaderCount", accountRepository.findByRole(Role.LEADER).size());
        model.addAttribute("recentLogs", auditService.getRecent());
        return "admin/dashboard";
    }
}
