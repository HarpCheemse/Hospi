package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.hotel.entity.Hotel;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.room.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import static com.hospi.manage.common.constant.Attributes.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the guest-facing rooms listing page.
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/rooms")
public class RoomPageController {
    private final RoomTypeService roomTypeService;
    private final HotelService hotelService;

    /**
     * Show the rooms page with all available room types.
     */
    @GetMapping
    public String rooms(Model model) {
        getHotelOrDefault(model);
        model.addAttribute(ROOM_TYPES,
                roomTypeService.findAllViews());
        return "guest/rooms";
    }

    @GetMapping("/{id}")
    public String roomDetail(@PathVariable Long id, Model model) {
        getHotelOrDefault(model);
        model.addAttribute("roomType", roomTypeService.findView(id));
        return "guest/rooms-detail";
    }

    private void getHotelOrDefault(Model model) {
        try {
            Hotel hotel = hotelService.find();
            model.addAttribute(HOTEL, hotel);
        } catch (DataAccessException | IllegalArgumentException e) {
            log.warn("Failed to load hotel details", e);
            model.addAttribute(HOTEL, null);
        }
    }
}
