package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    private final HotelService hotelService;

    public HomeController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public String home(Model model) {
        try {
            Hotel hotel = hotelService.find();
            model.addAttribute("hotel",
                    hotel);
            model.addAttribute("banners",
                    hotel.getPictures());
        } catch (Exception e) {
            model.addAttribute("hotel",
                    null);
            model.addAttribute("banners",
                    null);
        }
        return "guest/home";
    }
}
