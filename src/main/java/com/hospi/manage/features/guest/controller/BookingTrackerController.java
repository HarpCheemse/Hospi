package com.hospi.manage.features.guest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/my-booking")
public class BookingTrackerController {

    @GetMapping
    public String myBooking() {
        return "guest/my-booking";
    }
}
