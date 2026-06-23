package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.hospi.manage.common.constant.Attributes.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PageController {

    private final HotelService hotelService;

    @GetMapping("/")
    public String home(Model model) {
        getHotelOrDefault(model);
        return "guest/home";
    }

    @GetMapping("/policies")
    public String policies(Model model) {
        getHotelOrDefault(model);
        return "guest/policies";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        getHotelOrDefault(model);
        return "guest/contact";
    }

    private void getHotelOrDefault(Model model) {
        try {
            Hotel hotel = hotelService.find();
            model.addAttribute(HOTEL, hotel);
            model.addAttribute(BANNERS, hotel.getPictures());
        } catch (DataAccessException | IllegalArgumentException e) {
            log.warn("Failed to load hotel details", e);
            model.addAttribute(HOTEL, null);
            model.addAttribute(BANNERS, null);
        }
    }
}
