package com.hospi.manage.features.manager.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("activeSidebar", "DASHBOARD");
        return "manager/dashboard";
    }
}
