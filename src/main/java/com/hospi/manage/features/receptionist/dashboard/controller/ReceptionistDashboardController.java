package com.hospi.manage.features.receptionist.dashboard.controller;

import com.hospi.manage.common.constant.Attributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistDashboardController {
    @GetMapping
    String dashboard(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD");
        return "receptionist/dashboard";
    }
}
