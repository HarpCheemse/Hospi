package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.manager.detail.service.HotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/policies")
public class PolicyPageController {
    private final HotelService hotelService;

    public PolicyPageController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public String policies(Model model) {
        try {
            model.addAttribute("hotel",
                    hotelService.find());
        } catch (Exception e) {
            log.warn("Failed to load hotel details for policies page", e);
            model.addAttribute("hotel",
                    null);
        }
        return "guest/policies";
    }
}
