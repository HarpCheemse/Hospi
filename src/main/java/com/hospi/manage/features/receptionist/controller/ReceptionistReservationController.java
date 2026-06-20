package com.hospi.manage.features.receptionist.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.receptionist.dto.StayingGuestForm;
import com.hospi.manage.features.reservation.dto.DateSearchForm;
import com.hospi.manage.features.reservation.dto.OfflineBookingForm;
import com.hospi.manage.features.reservation.dto.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import com.hospi.manage.features.reservation.validator.OfflineBookingValidator;
import com.hospi.manage.features.room.dto.response.RoomSelection;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/receptionist/reservations")
public class ReceptionistReservationController {

    private final ReservationService reservationService;
    private final OfflineBookingValidator offlineBookingValidator;
    private final RoomAvailabilityService roomAvailabilityService;
    private final StayingGuestService stayingGuestService;
    private final RoomAssignmentService roomAssignmentService;

    public ReceptionistReservationController(ReservationService reservationService,
                                             OfflineBookingValidator offlineBookingValidator,
                                             RoomAvailabilityService roomAvailabilityService,
                                             StayingGuestService stayingGuestService,
                                             RoomAssignmentService roomAssignmentService) {
        this.reservationService = reservationService;
        this.offlineBookingValidator = offlineBookingValidator;
        this.roomAvailabilityService = roomAvailabilityService;
        this.stayingGuestService = stayingGuestService;
        this.roomAssignmentService = roomAssignmentService;
    }

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                "RESERVATIONS");
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("checkedInBookings",
                reservationService.findByStatus(ReservationStatus.CHECKED_IN));
        model.addAttribute("activeBookings",
                reservationService.findByStatuses(List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                )));
        return "receptionist/reservation/list";
    }

    @GetMapping("/create")
    String create(Model model) {
        model.addAttribute(Attributes.FORM,
                new DateSearchForm());
        return "receptionist/reservation/create";
    }

    @PostMapping("/create")
    String searchDates(@Valid @ModelAttribute(Attributes.FORM) DateSearchForm form, BindingResult binding,
                       Model model) {
        if (form.getCheckOutAt() != null && form.getCheckInAt() != null && !form.getCheckOutAt().isAfter(form.getCheckInAt())) {
            binding.rejectValue("checkOutAt",
                    "error",
                    "Check-out must be after check-in");
        }

        if (binding.hasErrors()) {
            return "receptionist/reservation/create";
        }

        return "redirect:/receptionist/reservations/create/details" + "?checkInAt=" + form.getCheckInAt() + "&checkOutAt=" + form.getCheckOutAt();
    }

    @GetMapping("/create/details")
    String createDetails(@RequestParam(required = false) LocalDate checkInAt,
                         @RequestParam(required = false) LocalDate checkOutAt, Model model) {
        if (checkInAt == null || checkOutAt == null) {
            return "redirect:/receptionist/reservations/create";
        }

        List<RoomTypeAvailabilityView> view = getAvailabilityView(
                checkInAt,
                checkOutAt
        );

        List<RoomSelection> roomSelections = view.stream().map(rt -> new RoomSelection(
                rt.roomTypeId(),
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
                view
        );
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

        offlineBookingValidator.validateCreate(
                form,
                bindingResult
        );

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    Attributes.VIEW,
                    getAvailabilityView(
                            form.checkInAt(),
                            form.checkOutAt()
                    )
            );

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
        model.addAttribute("reservation",
                reservation);
        model.addAttribute("guests",
                stayingGuestService.getGuests(id));

        var assignedRooms = roomAssignmentService.getAssignedRooms(id);
        model.addAttribute("assignedRooms",
                assignedRooms);
        model.addAttribute("availableRooms",
                roomAssignmentService.getAvailableRooms(id));

        var assignedCounts = assignedRooms.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getRoom().getRoomType().getId(),
                        Collectors.summingInt(a -> 1)
                ));
        model.addAttribute("assignedCounts",
                assignedCounts);

        if (edit != null) {
            var guest = stayingGuestService.getGuest(edit);
            var form = new StayingGuestForm();
            form.setGuestName(guest.getGuestName());
            form.setDateOfBirth(guest.getDateOfBirth());
            form.setNationality(guest.getNationality());
            model.addAttribute(Attributes.FORM,
                    form);
            model.addAttribute("editGuestId",
                    edit);
        } else {
            model.addAttribute(Attributes.FORM,
                    new StayingGuestForm());
        }

        return "receptionist/reservation/manage";
    }

    @PostMapping("/{id}/manage/guests")
    String addGuest(@PathVariable Long id,
                    @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form,
                    BindingResult binding, Model model) {
        var reservation = reservationService.findById(id);
        if (binding.hasErrors()) {
            model.addAttribute("reservation",
                    reservation);
            model.addAttribute("guests",
                    stayingGuestService.getGuests(id));
            model.addAttribute("assignedRooms",
                    roomAssignmentService.getAssignedRooms(id));
            model.addAttribute("availableRooms",
                    roomAssignmentService.getAvailableRooms(id));
            return "receptionist/reservation/manage";
        }
        stayingGuestService.addGuest(id,
                form);
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/guests/{guestId}")
    String editGuest(@PathVariable Long id, @PathVariable Long guestId,
                     @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form,
                     BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("reservation",
                    reservationService.findById(id));
            model.addAttribute("guests",
                    stayingGuestService.getGuests(id));
            model.addAttribute("assignedRooms",
                    roomAssignmentService.getAssignedRooms(id));
            model.addAttribute("availableRooms",
                    roomAssignmentService.getAvailableRooms(id));
            model.addAttribute("editGuestId",
                    guestId);
            return "receptionist/reservation/manage";
        }
        stayingGuestService.updateGuest(guestId,
                form);
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/guests/{guestId}/delete")
    String deleteGuest(@PathVariable Long id, @PathVariable Long guestId) {
        stayingGuestService.deleteGuest(guestId);
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/rooms")
    String assignRoom(@PathVariable Long id,
                      @RequestParam Long roomId,
                      Model model) {
        try {
            roomAssignmentService.assignRoom(id,
                    roomId);
        } catch (IllegalStateException e) {
            // ignore duplicate or already-occupied
        }
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    @PostMapping("/{id}/manage/rooms/{assignmentId}/remove")
    String removeRoom(@PathVariable Long id, @PathVariable Long assignmentId) {
        roomAssignmentService.removeAssignment(assignmentId);
        return "redirect:/receptionist/reservations/" + id + "/manage";
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

            redirect.addFlashAttribute("success",
                    "Stay extended successfully.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error",
                    e.getMessage());
        }

        return "redirect:/receptionist/reservations/" + id + "/manage";
    }
}
