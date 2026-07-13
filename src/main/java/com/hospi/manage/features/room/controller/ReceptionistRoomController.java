package com.hospi.manage.features.room.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.room.dto.response.RoomOccupancyView;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Controller for the receptionist room status overview page. */
@Controller
@RequestMapping("/receptionist/room-status")
@RequiredArgsConstructor
public class ReceptionistRoomController {

    private final RoomService roomService;
    private final HotelService hotelService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "ROOM_STATUS");
    }

    /** Show the room status overview page with floor-organized room grid. */
    @GetMapping
    String roomStatus(@RequestParam(required = false) Integer floor, Model model) {
        model.addAttribute(Attributes.HOTEL, hotelService.find());
        model.addAttribute(Attributes.FLOORS, roomService.getFloorViews(floor));
        model.addAttribute(Attributes.SELECTED_FLOOR, floor);
        return "receptionist/room-status";
    }

    /** Return occupancy details for a room as JSON (AJAX endpoint). */
    @GetMapping("/{id}/occupancy")
    @ResponseBody
    ResponseEntity<RoomOccupancyView> getRoomOccupancy(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomOccupancy(id));
    }

    /** Update a room's condition status (AJAX endpoint). */
    @PostMapping("/{id}/condition")
    @ResponseBody
    ResponseEntity<Void> updateCondition(@PathVariable Long id,
                                         @RequestParam ConditionStatus conditionStatus) {
        roomService.updateConditionStatus(id, conditionStatus);
        return ResponseEntity.ok().build();
    }
}
