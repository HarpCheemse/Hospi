package com.hospi.manage.features.receptionist.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.reservation.dto.DateSearchForm;
import com.hospi.manage.features.reservation.dto.OfflineBookingForm;
import com.hospi.manage.features.reservation.dto.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.reservation.validator.OfflineBookingValidator;
import com.hospi.manage.features.room.dto.RoomSelection;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/receptionist/reservations")
public class ReceptionistReservationController {

    private final ReservationService reservationService;
    private final OfflineBookingValidator offlineBookingValidator;
    private final RoomAvailabilityService roomAvailabilityService;

    public ReceptionistReservationController(ReservationService reservationService,
                                             OfflineBookingValidator offlineBookingValidator,
                                             RoomAvailabilityService roomAvailabilityService) {
        this.reservationService = reservationService;
        this.offlineBookingValidator = offlineBookingValidator;
        this.roomAvailabilityService = roomAvailabilityService;
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
}
