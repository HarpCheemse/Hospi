package com.hospi.manage.features.leader.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<Room> allRooms = roomService.findAll();
        long totalRooms = allRooms.stream()
                .filter(room -> room.getConditionStatus() != ConditionStatus.MAINTENANCE)
                .count();
        long cleanRooms = allRooms.stream()
                .filter(room -> room.getConditionStatus() == ConditionStatus.CLEAN)
                .count();

        model.addAttribute("dirtyRooms", roomService.getActiveDirtyRooms());
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("cleanRooms", cleanRooms);
        return "leader/dashboard";
    }

    @PostMapping("/rooms/{id}/clean")
    @ResponseBody
    public ResponseEntity<Void> cleanRoom(@PathVariable Long id) {
        roomService.updateConditionStatus(id, ConditionStatus.CLEAN);
        return ResponseEntity.ok().build();
    }
}
