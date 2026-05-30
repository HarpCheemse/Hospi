package com.hospi.manage.features.manager.room.controller;

import com.hospi.manage.common.constant.Attributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager/rooms")
public class RoomController {
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "ROOMS");
    }

    @GetMapping
    public String list(Model model) {
        return "manager/room/list";
    }
}
