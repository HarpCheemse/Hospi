package com.hospi.manage.features.manager.room.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.manager.detail.service.HotelService;
import com.hospi.manage.features.room.dto.RoomCreateForm;
import com.hospi.manage.features.room.dto.RoomEditForm;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.service.RoomService;
import com.hospi.manage.features.room.service.RoomTypeService;
import com.hospi.manage.features.room.validation.RoomValidator;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/rooms")
public class RoomController {
    final private HotelService hotelService;
    final private RoomTypeService roomTypeService;
    final private RoomService roomService;

    final private RoomValidator roomValidator;

    RoomController(HotelService hotelService,
                   RoomTypeService roomTypeService,
                   RoomService roomService,
                   RoomValidator roomValidator) {
        this.hotelService = hotelService;
        this.roomTypeService = roomTypeService;
        this.roomService = roomService;
        this.roomValidator = roomValidator;
    }


    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "ROOMS");
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hotel",
                hotelService.find());
        model.addAttribute("floors",
                roomService.getFloorViews());
        return "manager/room/list";
    }

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

    @PostMapping("/generate-layout")
    public String generateDefaultRoomLayout(
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        boolean result = roomService.generateDefaultRoomLayout();

        if (result) {
            return "redirect:/manager/rooms";
        }
        redirectAttributes.addAttribute(Attributes.ERROR,
                "Rooms List must be empty to use this function");
        return "redirect:/manager/rooms";
    }
}
