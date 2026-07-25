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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    /** Show the revenue reports page filtered by year and optionally month. */
    @GetMapping
    String revenue(@RequestParam(required = false) Integer year,
                   @RequestParam(required = false) Integer month,
                   Model model) {
        int y = year != null ? year : LocalDate.now().getYear();
        LocalDate start, end;
        String period;

        if (month != null) {
            start = LocalDate.of(y, month, 1);
            end = start.withDayOfMonth(start.lengthOfMonth());
            period = start.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
        } else {
            start = LocalDate.of(y, 1, 1);
            end = LocalDate.of(y, 12, 31);
            period = String.valueOf(y);
        }

        model.addAttribute(Attributes.VIEW, revenueService.getRevenueView(start, end, period));
        model.addAttribute(Attributes.SELECTED_YEAR, y);
        model.addAttribute(Attributes.SELECTED_MONTH, month);
        return "manager/revenues";
    }
}
