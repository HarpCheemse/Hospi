package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.room.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import static com.hospi.manage.common.constant.Attributes.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the guest-facing rooms listing page.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/rooms")
public class RoomPageController {
    private final RoomTypeService roomTypeService;

    /**
     * Show the rooms page with all available room types.
     */
    @GetMapping
    public String rooms(Model model) {
        model.addAttribute(ROOM_TYPES,
                roomTypeService.findAllViews());
        return "guest/rooms";
    }
}
