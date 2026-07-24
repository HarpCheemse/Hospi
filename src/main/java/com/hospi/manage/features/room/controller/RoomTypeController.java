package com.hospi.manage.features.room.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.room.dto.request.RoomTypeCreateForm;
import com.hospi.manage.features.room.dto.request.RoomTypeEditForm;
import com.hospi.manage.features.room.dto.response.RoomTypeCreateView;
import com.hospi.manage.features.room.dto.response.RoomTypeView;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.service.RoomTypeService;
import com.hospi.manage.features.room.validation.CreateRoomTypeValidator;
import com.hospi.manage.features.room.validation.EditRoomTypeValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Controller for room type management (CRUD with image upload).
 */
@Controller
@RequestMapping("/manager/room-types")
@RequiredArgsConstructor
@Slf4j
public class RoomTypeController {
    private final RoomTypeService roomTypeService;
    private final CreateRoomTypeValidator createRoomTypeValidator;
    private final EditRoomTypeValidator editRoomTypeValidator;

    /**
     * Set the active sidebar highlight for this feature.
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "ROOM_TYPES");
    }


    /**
     * Show the room type listing page.
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute(Attributes.VIEW,
                roomTypeService.findAllViews());
        return "manager/room-type/list";
    }

    /**
     * Show the room type creation form.
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute(Attributes.FORM,
                RoomTypeCreateForm.empty());

        model.addAttribute(Attributes.VIEW,
                RoomTypeCreateView.defaultView());

        return "manager/room-type/create";
    }

    /**
     * Create a new room type with optional cover and gallery images. Redirects to
     * the room type list on success.
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute(Attributes.FORM) RoomTypeCreateForm form, BindingResult errors,
                         Model model) throws IOException {

        log.info("Create room type POST received: category={}, tier={}, features='{}', price={}, coverImage={}",
                form.category(), form.tier(), form.features(), form.basePrice(),
                form.coverImage() != null ? form.coverImage().getOriginalFilename() : "null");

        createRoomTypeValidator.validate(form,
                errors);

        if (errors.hasErrors()) {
            log.warn("Validation failed: {}", errors.getAllErrors());
            model.addAttribute(Attributes.FORM,
                    form);
            model.addAttribute(Attributes.VIEW,
                    RoomTypeCreateView.defaultView());

            return "manager/room-type/create";
        }

        log.info("Validation passed, calling service...");
        roomTypeService.createRoomType(form);
        log.info("Room type created successfully");

        return "redirect:/manager/room-types";
    }

    /**
     * Show details for a single room type.
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute(Attributes.VIEW,
                roomTypeService.findViewById(id));

        return "manager/room-type/detail";
    }

    /**
     * Show the room type edit form pre-populated with current values.
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var roomType = roomTypeService.findById(id);

        model.addAttribute(Attributes.FORM,
                RoomTypeEditForm.from(roomType));

        model.addAttribute(Attributes.VIEW,
                RoomTypeView.from(roomType));

        return "manager/room-type/edit";
    }

    /**
     * Update an existing room type with optional image changes. Redirects to the
     * room type detail page on success.
     */
    @PostMapping("/{id}/edit")
    public String updateRoomType(@PathVariable Long id, @Valid @ModelAttribute(Attributes.FORM) RoomTypeEditForm form,
                                 BindingResult errors, Model model) throws IOException {
        RoomType roomType = roomTypeService.findById(id);
        int existingImageCount = (int) roomType.getPictures().stream().filter(p -> !Integer.valueOf(1).equals(p.getSortOrder())).count();

        editRoomTypeValidator.validate(id,
                form,
                existingImageCount,
                errors);

        if (errors.hasErrors()) {
            model.addAttribute(Attributes.FORM,
                    form);
            model.addAttribute(Attributes.VIEW,
                    RoomTypeView.from(roomType));

            return "manager/room-type/edit";
        }

        roomTypeService.update(id,
                form);

        return "redirect:/manager/room-types/" + id;
    }

    /**
     * Delete a room type by ID. Redirects to the list, or back to edit with an
     * error flash if deletion is not allowed.
     */
    @PostMapping("/delete/{id}")
    public String deleteRoomType(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            roomTypeService.delete(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
            return "redirect:/manager/room-types/" + id + "/edit";
        }

        return "redirect:/manager/room-types";
    }
}
