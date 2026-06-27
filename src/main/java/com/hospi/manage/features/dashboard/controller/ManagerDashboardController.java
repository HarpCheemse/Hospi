package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.common.constant.Attributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the manager dashboard landing page.
 */
@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {
    /**
     * Show the manager dashboard page.
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD");
        return "manager/dashboard";
    }
}
