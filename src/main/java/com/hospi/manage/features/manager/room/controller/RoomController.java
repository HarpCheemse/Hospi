package com.hospi.manage.features.manager.room.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.service.HotelService;
import com.hospi.manage.features.manager.room.service.RoomService;
import com.hospi.manage.features.manager.roomtype.entity.RoomType;
import com.hospi.manage.features.manager.roomtype.service.RoomTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/rooms")
public class RoomController {
    final private HotelService hotelService;
    final private RoomTypeService roomTypeService;
    final private RoomService roomService;

    RoomController(HotelService hotelService,
                   RoomTypeService roomTypeService, RoomService roomService) {
        this.hotelService = hotelService;
        this.roomTypeService = roomTypeService;
        this.roomService = roomService;
    }


    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "ROOMS");
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hotel", hotelService.findById(null));
        model.addAttribute("floors",roomService.getFloorViews());
        return "manager/room/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("hotel", hotelService.findById(null));
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "manager/room/create";
    }

    @PostMapping("/create")
    public String createRoom(@RequestParam int floor,
                             @RequestParam("count") int numberOfRooms,
                             @RequestParam Long roomTypeId) {

        roomService.createRoom(floor, numberOfRooms, roomTypeId);
        return "redirect:/manager/rooms";
    }
}
