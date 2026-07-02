package com.hospi.manage.features.hotel.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.hotel.dto.HotelForm;
import com.hospi.manage.features.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Controller for managing hotel details (name, address, images).
 */
@Controller
@RequestMapping("/manager/details")
@RequiredArgsConstructor
public class HotelController {
    private final HotelService hotelService;

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
        return "hotel/detail";
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
        return "hotel/edit";
    }

    /**
     * Update hotel details and manage images (cover, gallery additions, removals).
     */
    @PostMapping("/edit")
    public String updateDetail(@ModelAttribute HotelForm form,
                                @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                                @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
                                @RequestParam(value = "removeImageIds", required = false) List<Long> removeImageIds,
                                RedirectAttributes redirect)
            throws IOException {
        hotelService.update(form, coverImage, newImages, removeImageIds);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Hotel details updated.");
        return "redirect:/manager/details";
    }
}
