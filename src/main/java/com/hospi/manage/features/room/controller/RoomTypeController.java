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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/manager/room-types")
public class RoomTypeController {
    private final RoomTypeService roomTypeService;
    private final CreateRoomTypeValidator createRoomTypeValidator;
    private final EditRoomTypeValidator editRoomTypeValidator;

    public RoomTypeController(RoomTypeService roomTypeService, CreateRoomTypeValidator createRoomTypeValidator,
                              EditRoomTypeValidator editRoomTypeValidator) {
        this.roomTypeService = roomTypeService;
        this.createRoomTypeValidator = createRoomTypeValidator;
        this.editRoomTypeValidator = editRoomTypeValidator;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "ROOM_TYPES");
    }


    @GetMapping
    public String list(Model model) {
        model.addAttribute(Attributes.VIEW,
                roomTypeService.findAllViews());
        return "manager/room-type/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute(Attributes.FORM,
                RoomTypeCreateForm.empty());

        model.addAttribute(Attributes.VIEW,
                RoomTypeCreateView.defaultView());

        return "manager/room-type/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute(Attributes.FORM) RoomTypeCreateForm form, BindingResult errors,
                         Model model) throws IOException {

        createRoomTypeValidator.validate(form,
                errors);

        if (errors.hasErrors()) {
            model.addAttribute(Attributes.FORM,
                    form);
            model.addAttribute(Attributes.VIEW,
                    RoomTypeCreateView.defaultView());

            return "manager/room-type/create";
        }

        roomTypeService.createRoomType(form);

        return "redirect:/manager/room-types";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute(Attributes.VIEW,
                roomTypeService.findViewById(id));

        return "manager/room-type/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var roomType = roomTypeService.findById(id);

        model.addAttribute(Attributes.FORM,
                RoomTypeEditForm.from(roomType));

        model.addAttribute(Attributes.VIEW,
                RoomTypeView.from(roomType));

        return "manager/room-type/edit";
    }

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
