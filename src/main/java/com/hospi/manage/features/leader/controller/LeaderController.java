package com.hospi.manage.features.leader.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/leader")
@RequiredArgsConstructor
public class LeaderController {

    private final RoomService roomService;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "TASKS");
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("dirtyRooms", roomService.getActiveDirtyRooms());
        return "leader/dashboard";
    }

    @PostMapping("/rooms/{id}/clean")
    @ResponseBody
    public ResponseEntity<Void> cleanRoom(@PathVariable Long id) {
        roomService.updateConditionStatus(id, ConditionStatus.CLEAN);
        return ResponseEntity.ok().build();
    }
}
