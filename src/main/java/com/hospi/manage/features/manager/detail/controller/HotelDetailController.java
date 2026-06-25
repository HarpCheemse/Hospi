package com.hospi.manage.features.manager.detail.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.manager.detail.dto.HotelForm;
import com.hospi.manage.features.manager.detail.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller for managing hotel details (name, address, images).
 */
@Controller
@RequestMapping("/manager/details")
public class HotelDetailController {
    private final HotelService hotelService;

    HotelDetailController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * Set the active sidebar highlight for this feature.
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "HOTEL_DETAILS");
    }

    /**
     * Show the hotel details page.
     */
    @GetMapping
    public String detail(Model model) {
        model.addAttribute("hotel",
                hotelService.find());
        return "manager/detail/detail";
    }

    /**
     * Show the hotel details edit form pre-populated with current values.
     */
    @GetMapping("/edit")
    public String edit(Model model) {
        model.addAttribute("hotelForm",
                hotelService.getForm());
        model.addAttribute("hotel",
                hotelService.find());
        return "manager/detail/edit";
    }

    /**
     * Update hotel details and optionally replace banner images, then redirect to
     * the details page.
     */
    @PostMapping("/edit")
    public String updateDetail(@ModelAttribute HotelForm form,
                               @RequestParam(value = "images", required = false) MultipartFile images)
            throws IOException {
        hotelService.update(form,
                images);
        return "redirect:/manager/details";
    }
}
