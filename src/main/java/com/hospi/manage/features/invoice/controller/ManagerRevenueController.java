package com.hospi.manage.features.invoice.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.invoice.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Controller for the manager revenue reports page. */
@Controller
@RequestMapping("/manager/revenues")
@RequiredArgsConstructor
public class ManagerRevenueController {

    private final RevenueService revenueService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "REVENUES");
    }

    /** Show the revenue reports page filtered by year. */
    @GetMapping
    String revenue(@RequestParam(required = false) Integer year, Model model) {
        int y = year != null ? year : LocalDate.now().getYear();
        LocalDate start = LocalDate.of(y, 1, 1);
        LocalDate end = LocalDate.of(y, 12, 31);
        model.addAttribute(Attributes.VIEW, revenueService.getRevenueView(start, end, String.valueOf(y)));
        model.addAttribute("selectedYear", y);
        return "manager/revenues";
    }
}
