package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import static com.hospi.manage.common.constant.Attributes.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/contact")
public class ContactPageController {
    private final HotelService hotelService;

    @GetMapping
    public String contact(Model model) {
        try {
            Hotel hotel = hotelService.find();
            model.addAttribute(HOTEL,
                    hotel);
        } catch (Exception e) {
            log.warn("Failed to load hotel details for contact page", e);
            model.addAttribute(HOTEL,
                    null);
        }
        return "guest/contact";
    }
}
