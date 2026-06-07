package com.hospi.manage.features.admin.dashboard;

import com.hospi.manage.common.constant.Attributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "DASHBOARD");
        return "admin/dashboard";
    }
}
