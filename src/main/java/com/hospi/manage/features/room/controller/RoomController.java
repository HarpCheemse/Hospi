package com.hospi.manage.features.room.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.hotel.service.HotelService;
import com.hospi.manage.features.room.dto.request.RoomCreateForm;
import com.hospi.manage.features.room.dto.request.RoomEditForm;
import com.hospi.manage.features.room.dto.response.RoomOccupancyView;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.service.RoomService;
import com.hospi.manage.features.room.service.RoomTypeService;
import com.hospi.manage.features.room.validation.RoomValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for room management (list, create, edit, delete, occupancy, condition).
 */
@Controller
@RequestMapping("/manager/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final HotelService hotelService;
    private final RoomTypeService roomTypeService;
    private final RoomService roomService;
    private final RoomValidator roomValidator;

    /**
     * Set the active sidebar highlight for this feature.
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "ROOMS");
    }

    /**
     * Show the room listing page, optionally filtered by floor.
     */
    @GetMapping
    public String list(@RequestParam(required = false) Integer floor,
                       Model model) {
        model.addAttribute("hotel",
                hotelService.find());
        model.addAttribute("floors",
                roomService.getFloorViews(floor));
        model.addAttribute("selectedFloor",
                floor);
        return "manager/room/list";
    }

    /**
     * Show the room creation form.
     */
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute(Attributes.FORM,
                new RoomCreateForm(null,
                        null,
                        null));

        model.addAttribute("hotel",
                hotelService.find());
        model.addAttribute("roomTypes",
                roomTypeService.findAll());
        return "manager/room/create";
    }

    /**
     * Show the room edit form pre-populated with current values.
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Room room = roomService.findById(id);

        model.addAttribute(Attributes.FORM,
                new RoomEditForm(room.getRoomNumber(),
                        room.getRoomType().getId(),
                        room.getConditionStatus()));

        model.addAttribute("conditionStatuses",
                ConditionStatus.values());

        model.addAttribute("roomTypes",
                roomTypeService.findAll());

        model.addAttribute("floor",
                room.getFloorNumber());
        model.addAttribute("roomId",
                room.getId());
        return "manager/room/edit";
    }


    /**
     * Create new rooms on a floor. Validates input, persists the rooms, and
     * redirects to the room list.
     */
    @PostMapping("/create")
    public String createRoom(@Valid @ModelAttribute(Attributes.FORM) RoomCreateForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        roomValidator.validateCreate(form,
                bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("hotel",
                    hotelService.find());
            model.addAttribute("roomTypes",
                    roomTypeService.findAll());

            return "manager/room/create";
        }

        roomService.createRoom(form);
        redirectAttributes.addFlashAttribute(
                Attributes.SUCCESS,
                "Create " + form.numberOfRooms() + " rooms on floor "
                        + form.floor() + " successfully"
        );
        return "redirect:/manager/rooms";
    }

    /**
     * Update an existing room's details. Validates, persists, and redirects to
     * the room list.
     */
    @PostMapping("/{id}/edit")
    public String editRoom(@PathVariable Long id,
                           @Valid @ModelAttribute(Attributes.FORM) RoomEditForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        Room room = roomService.findById(id);

        roomValidator.validateUpdate(room,
                form,
                bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("roomTypes",
                    roomTypeService.findAll());
            model.addAttribute("floor",
                    room.getFloorNumber());
            model.addAttribute("roomId",
                    room.getId());
            model.addAttribute("conditionStatuses",
                    ConditionStatus.values());
            return "manager/room/edit";
        }

        roomService.updateRoom(id,
                form);

        redirectAttributes.addFlashAttribute(
                Attributes.SUCCESS,
                "Room updated successfully"
        );
        return "redirect:/manager/rooms";
    }

    /**
     * Return occupancy details for a room as JSON (AJAX endpoint).
     */
    @GetMapping("/{id}/occupancy")
    @ResponseBody
    public ResponseEntity<RoomOccupancyView> getRoomOccupancy(@PathVariable Long id) {
        RoomOccupancyView occupancy = roomService.getRoomOccupancy(id);
        return ResponseEntity.ok(occupancy);
    }

    /**
     * Update a room's condition status (AJAX endpoint).
     */
    @PostMapping("/{id}/condition")
    @ResponseBody
    public ResponseEntity<Void> updateCondition(@PathVariable Long id,
                                                @RequestParam ConditionStatus conditionStatus) {
        roomService.updateConditionStatus(id,
                conditionStatus);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a room by ID. Redirects to the room list, or back to edit with an
     * error flash if deletion is not allowed.
     */
    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            roomService.delete(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
            return "redirect:/manager/rooms/" + id + "/edit";
        }

        return "redirect:/manager/rooms";
    }
}
