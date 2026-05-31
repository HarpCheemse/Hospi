package com.hospi.manage.features.manager.detail.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.manager.detail.dto.HotelForm;
import com.hospi.manage.features.manager.detail.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/manager/details")
public class HotelDetailController {
    private final HotelService hotelService;

    HotelDetailController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "HOTEL_DETAILS");
    }

    @GetMapping
    public String detail(Model model) {
        model.addAttribute("hotel", hotelService.findById(null));
        return "manager/detail/detail";
    }

    @GetMapping("/edit")
    public String edit(Model model) {
        model.addAttribute("hotelForm", hotelService.getForm(null));
        model.addAttribute("hotel", hotelService.findById(null));
        return "manager/detail/edit";
    }

    @PostMapping("/edit")
    public String updateDetail(@ModelAttribute HotelForm form,
            @RequestParam(value = "images", required = false) MultipartFile images) throws IOException {
        hotelService.update(form, images);
        return "redirect:/manager/details";
    }
}
