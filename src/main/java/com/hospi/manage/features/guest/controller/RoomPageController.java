package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.room.service.RoomTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rooms")
public class RoomPageController {
    private final RoomTypeService roomTypeService;

    public RoomPageController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public String rooms(Model model) {
        model.addAttribute("roomTypes",
                roomTypeService.findAll());
        return "guest/rooms";
    }
}
