package com.hospi.manage.features.guest.homepage.controller;

import com.hospi.manage.features.manager.detail.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
            model.addAttribute("hotel", hotelService.findById(null));
        } catch (Exception e) {
            model.addAttribute("hotel", null);
        }
        return "guest/policies";
    }
}
