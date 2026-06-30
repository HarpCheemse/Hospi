package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the receptionist dashboard landing page.
 */
@Controller
@RequestMapping("/receptionist")
public class DashboardController {
    /**
     * Show the receptionist dashboard page.
     */
    @GetMapping
    String dashboard(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "DASHBOARD");
        return "receptionist/dashboard";
    }
}
