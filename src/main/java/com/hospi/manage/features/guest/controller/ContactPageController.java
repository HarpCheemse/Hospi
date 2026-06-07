package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contact")
public class ContactPageController {
    private final HotelService hotelService;

    public ContactPageController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public String contact(Model model) {
        try {
            Hotel hotel = hotelService.findById(null);
            model.addAttribute("hotel", hotel);
        } catch (Exception e) {
            model.addAttribute("hotel", null);
        }
        return "guest/contact";
    }
}
