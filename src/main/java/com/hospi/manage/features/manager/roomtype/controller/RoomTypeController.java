package com.hospi.manage.features.manager.roomtype.controller;

import com.hospi.manage.common.utils.ImageUtils;
import com.hospi.manage.features.manager.roomtype.dto.RoomTypeForm;
import com.hospi.manage.features.manager.roomtype.entity.RoomType;
import com.hospi.manage.features.manager.roomtype.enums.BedType;
import com.hospi.manage.features.manager.roomtype.service.RoomTypePictureService;
import com.hospi.manage.features.manager.roomtype.service.RoomTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/manager/room-types")
public class RoomTypeController {
    private final RoomTypeService roomTypeService;
    private final RoomTypePictureService roomTypePictureService;

    public RoomTypeController(RoomTypeService roomTypeService, RoomTypePictureService roomTypePictureService) {
        this.roomTypeService = roomTypeService;
        this.roomTypePictureService = roomTypePictureService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "manager/room-type/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("roomTypeForm", new RoomTypeForm());
        model.addAttribute("bedTypes", BedType.values());
        return "manager/room-type/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute RoomTypeForm form, @RequestParam("image") MultipartFile image)
            throws IOException {
        roomTypeService.create(form, image);

        return "redirect:/manager/room-types";

    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("roomType", roomTypeService.findById(id));

        return "manager/room-type/detail";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("roomType", roomTypeService.findById(id));
        model.addAttribute("bedTypes", BedType.values());
            
        return "manager/room-type/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateRoomType(@PathVariable Long id, @ModelAttribute RoomType form,
                                 @RequestParam(value = "images", required = false) MultipartFile images)
    throws IOException {
        roomTypeService.update(id, form, images);

        return "redirect:/manager/room-types";
    }

}
