package com.hospi.manage.features.leader.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.leader.mapper.LeaderDashboardMapper;
import com.hospi.manage.features.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.hospi.manage.features.room.entity.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Controller
@RequestMapping("/leader")
@RequiredArgsConstructor
public class LeaderController {

    private final RoomService roomService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "DASHBOARD");
        var allRooms = roomService.findAll();
        var view = LeaderDashboardMapper.toDashboardView(allRooms);
        model.addAttribute(Attributes.VIEW, view);
        return "leader/dashboard";
    }

    @GetMapping("/tasks")
    public String tasks(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "TASKS");
        var dirtyRooms = roomService.getActiveDirtyRooms();
        var allRooms = roomService.findAll();
        long totalRooms = allRooms.stream()
                .filter(r -> r.getConditionStatus() != com.hospi.manage.features.room.enums.ConditionStatus.MAINTENANCE)
                .count();
        long cleanRooms = allRooms.stream()
                .filter(r -> r.getConditionStatus() == com.hospi.manage.features.room.enums.ConditionStatus.CLEAN)
                .count();

        var dashboardView = LeaderDashboardMapper.toDashboardView(allRooms);
        model.addAttribute("occupiedDirtyRooms", dashboardView.occupiedDirtyRooms());
        model.addAttribute("priorityRooms", dashboardView.priorityRooms());

        var roomsByFloor = new TreeMap<Short, List<Room>>();
        for (var room : dirtyRooms) {
            if (room.getOccupancyStatus() != com.hospi.manage.features.room.enums.OccupancyStatus.OCCUPIED) {
                roomsByFloor.computeIfAbsent(room.getFloorNumber(), k -> new ArrayList<>()).add(room);
            }
        }

        model.addAttribute("roomsByFloor", roomsByFloor);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("cleanRooms", cleanRooms);
        return "leader/tasks";
    }

    @PostMapping("/rooms/{id}/clean")
    @ResponseBody
    public ResponseEntity<Void> cleanRoom(@PathVariable Long id) {
        roomService.updateConditionStatus(id, com.hospi.manage.features.room.enums.ConditionStatus.CLEAN);
        return ResponseEntity.ok().build();
    }
}
