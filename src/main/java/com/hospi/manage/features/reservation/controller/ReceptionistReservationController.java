package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.reservation.dto.*;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.mapper.ReservationMapper;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import com.hospi.manage.features.reservation.validation.DateSearchValidator;
import com.hospi.manage.features.reservation.validation.OfflineBookingValidator;
import com.hospi.manage.features.room.dto.response.RoomSelection;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class ReceptionistReservationController {

    private final ReservationService reservationService;
    private final OfflineBookingValidator offlineBookingValidator;
    private final DateSearchValidator dateSearchValidator;
    private final RoomAvailabilityService roomAvailabilityService;
    private final StayingGuestService stayingGuestService;
    private final RoomAssignmentService roomAssignmentService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "RESERVATIONS");
    }

    @GetMapping
    String list(@RequestParam(required = false) String status,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                @RequestParam(required = false) String search,
                @RequestParam(name = "checkedInSearch", required = false) String checkedInSearch,
                @RequestParam(name = "checkedInPage", defaultValue = "0") int checkedInPage,
                @RequestParam(name = "activePage", defaultValue = "0") int activePage,
                Model model) {
        List<ReservationStatus> statuses;
        if (status != null && !status.isBlank()) {
            statuses = List.of(ReservationStatus.valueOf(status));
        } else {
            statuses = List.of(ReservationStatus.PENDING,
                    ReservationStatus.CONFIRMED);
        }

        model.addAttribute(Attributes.VIEW,
                ReservationMapper.toListView(
                        reservationService.findCheckedInFiltered(checkedInSearch,
                                PageRequest.of(checkedInPage, 10)),
                        reservationService.findFiltered(statuses,
                                search,
                                date,
                                PageRequest.of(activePage, 20)),
                        status,
                        date,
                        search,
                        checkedInSearch));
        return "receptionist/reservation/list";
    }

    @GetMapping("/create")
    String create(Model model) {
        model.addAttribute(Attributes.FORM,
                new DateSearchForm(null,
                        null));
        return "receptionist/reservation/create";
    }

    @PostMapping("/create")
    String searchDates(@Valid @ModelAttribute(Attributes.FORM) DateSearchForm form, BindingResult binding,
                       Model model) {
        dateSearchValidator.validate(form,
                binding);

        if (binding.hasErrors()) {
            return "receptionist/reservation/create";
        }

        return UriComponentsBuilder.fromPath("/receptionist/reservations/create/details")
                .queryParam("checkInAt",
                        form.checkInAt())
                .queryParam("checkOutAt",
                        form.checkOutAt())
                .build().toUriString();
    }

    @GetMapping("/create/details")
    String createDetails(@RequestParam(required = false) LocalDate checkInAt,
                         @RequestParam(required = false) LocalDate checkOutAt, Model model) {
        if (checkInAt == null || checkOutAt == null) {
            return "redirect:/receptionist/reservations/create";
        }

        List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(
                checkInAt,
                checkOutAt);

        long nights = ChronoUnit.DAYS.between(checkInAt,
                checkOutAt);

        List<RoomSelection> roomSelections = availabilityView.stream()
                .map(rt -> new RoomSelection(rt.roomTypeId(),
                        0)).toList();

        OfflineBookingForm form = new OfflineBookingForm(checkInAt,
                checkOutAt,
                null,
                null,
                null,
                null,
                null,
                roomSelections);

        model.addAttribute(Attributes.VIEW,
                new CreateDetailsView(availabilityView,
                        nights));
        model.addAttribute(Attributes.FORM,
                form);

        return "receptionist/reservation/details";
    }

    @PostMapping("/create/details")
    String createBooking(
            @Valid @ModelAttribute(Attributes.FORM)
            OfflineBookingForm form,
            BindingResult bindingResult,
            RedirectAttributes redirect,
            Model model
    ) {

        offlineBookingValidator.validate(
                form,
                bindingResult
        );

        if (bindingResult.hasErrors()) {
            List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(
                    form.checkInAt(),
                    form.checkOutAt());
            long nights = ChronoUnit.DAYS.between(form.checkInAt(),
                    form.checkOutAt());
            model.addAttribute(Attributes.VIEW,
                    new CreateDetailsView(availabilityView,
                            nights));

            return "receptionist/reservation/details";
        }

        reservationService.createReservation(form);

        redirect.addFlashAttribute(
                Attributes.SUCCESS,
                "Offline booking created successfully."
        );

        return "redirect:/receptionist/reservations";
    }

    @GetMapping("/{id}/manage")
    String manage(@PathVariable Long id,
                  @RequestParam(required = false) Long edit,
                  Model model) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            return "redirect:/receptionist/reservations";
        }

        model.addAttribute(Attributes.VIEW,
                buildManageView(id,
                        reservation));

        if (edit != null) {
            var guest = stayingGuestService.getGuest(id,
                    edit);
            model.addAttribute(Attributes.FORM,
                    new StayingGuestForm(
                            guest.getGuestName(),
                            guest.getDateOfBirth(),
                            guest.getNationality()));
            model.addAttribute("editGuestId",
                    edit);
        } else {
            model.addAttribute(Attributes.FORM,
                    new StayingGuestForm(null,
                            null,
                            null));
        }

        return "receptionist/reservation/manage";
    }

    @PostMapping("/{id}/manage/guests")
    String addGuest(@PathVariable Long id,
                    @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form,
                    BindingResult binding, Model model,
                    RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute(Attributes.VIEW,
                    buildManageView(id,
                            reservationService.findById(id)));
            return "receptionist/reservation/manage";
        }
        stayingGuestService.addGuest(id,
                form);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Guest added successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/guests/{guestId}")
    String editGuest(@PathVariable Long id, @PathVariable Long guestId,
                     @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form,
                     BindingResult binding, Model model,
                     RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute(Attributes.VIEW,
                    buildManageView(id,
                            reservationService.findById(id)));
            model.addAttribute("editGuestId",
                    guestId);
            return "receptionist/reservation/manage";
        }
        stayingGuestService.updateGuest(id,
                guestId,
                form);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Guest updated successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/guests/{guestId}/delete")
    String deleteGuest(@PathVariable Long id, @PathVariable Long guestId,
                       RedirectAttributes redirect) {
        stayingGuestService.deleteGuest(id,
                guestId);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Guest removed successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/rooms")
    String assignRoom(@PathVariable Long id,
                      @RequestParam Long roomId,
                      RedirectAttributes redirect) {
        try {
            roomAssignmentService.assignRoom(id,
                    roomId);
            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Room assigned successfully.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/rooms/{assignmentId}/remove")
    String removeRoom(@PathVariable Long id, @PathVariable Long assignmentId,
                      RedirectAttributes redirect) {
        roomAssignmentService.removeAssignment(id,
                assignmentId);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Room assignment removed successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    private ManageReservationView buildManageView(Long id, Reservation reservation) {
        return ReservationMapper.toManageView(
                reservation,
                stayingGuestService.getGuests(id),
                roomAssignmentService.getAssignedRooms(id),
                roomAssignmentService.getAvailableRooms(id));
    }

    private List<RoomTypeAvailabilityView> getAvailabilityView(
            LocalDate checkInAt,
            LocalDate checkOutAt
    ) {
        return roomAvailabilityService.getAvailability(
                        checkInAt,
                        checkOutAt
                )
                .stream()
                .map(RoomTypeAvailabilityView::new)
                .toList();
    }

    @PostMapping("/{id}/manage/extend")
    public String extendStay(@PathVariable Long id,
                             @RequestParam int extraDays,
                             RedirectAttributes redirect) {

        try {
            reservationService.extendStay(id,
                    extraDays);

            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Stay extended successfully.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }

        return "redirect:/receptionist/reservations/" + id + "/manage";
    }
}
