package com.hospi.manage.features.guest.controller;

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
@RequestMapping("/policies")
public class PolicyPageController {
    private final HotelService hotelService;

    @GetMapping
    public String policies(Model model) {
        try {
            model.addAttribute(HOTEL,
                    hotelService.find());
        } catch (Exception e) {
            log.warn("Failed to load hotel details for policies page", e);
            model.addAttribute(HOTEL,
                    null);
        }
        return "guest/policies";
    }
}
