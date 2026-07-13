package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import com.hospi.manage.features.reservation.dto.request.OfflineBookingForm;
import com.hospi.manage.features.reservation.dto.response.CreateDetailsView;
import com.hospi.manage.features.reservation.dto.response.RoomTypeAvailabilityView;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.mapper.ReservationMapper;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.reservation.validation.DateSearchValidator;
import com.hospi.manage.features.reservation.validation.OfflineBookingFormValidator;
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

/** Controller for the receptionist active bookings listing and creation flow. */
@Controller
@RequestMapping("/receptionist/bookings")
@RequiredArgsConstructor
public class ReceptionistBookingController {

    private final ReservationService reservationService;
    private final OfflineBookingFormValidator offlineBookingFormValidator;
    private final DateSearchValidator dateSearchValidator;
    private final RoomAvailabilityService roomAvailabilityService;

    private static final int PAGE_SIZE = 10;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, Attributes.ACTIVE_BOOKINGS);
    }

    @GetMapping
    String activeBookings(@RequestParam(required = false) String status,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          @RequestParam(required = false) String search,
                          @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        List<ReservationStatus> statuses = (status != null && !status.isBlank())
                ? List.of(ReservationStatus.valueOf(status))
                : List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        model.addAttribute(Attributes.VIEW,
                ReservationMapper.toActiveBookingsView(reservationService.findFiltered(
                        statuses, search, date, PageRequest.of(page, PAGE_SIZE)), status, date, search));
        return "receptionist/reservation/active";
    }

    @GetMapping("/create")
    String create(Model model) {
        model.addAttribute(Attributes.FORM, new DateSearchForm(null, null));
        return "receptionist/reservation/create";
    }

    @PostMapping("/create")
    String searchDates(@Valid @ModelAttribute(Attributes.FORM) DateSearchForm form, BindingResult binding) {
        dateSearchValidator.validate(form, binding);
        if (binding.hasErrors()) {
            return "receptionist/reservation/create";
        }
        return "redirect:" + UriComponentsBuilder.fromPath("/receptionist/bookings/create/details")
                .queryParam("checkInAt", form.checkInAt())
                .queryParam("checkOutAt", form.checkOutAt()).build().toUriString();
    }

    @GetMapping("/create/details")
    String createDetails(@RequestParam(required = false) LocalDate checkInAt,
                         @RequestParam(required = false) LocalDate checkOutAt, Model model) {
        if (checkInAt == null || checkOutAt == null) {
            return "redirect:/receptionist/bookings/create";
        }

        List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(checkInAt, checkOutAt);
        long nights = ChronoUnit.DAYS.between(checkInAt, checkOutAt);

        List<RoomSelection> roomSelections = availabilityView.stream()
                .map(rt -> new RoomSelection(rt.roomTypeId(), 0)).toList();

        model.addAttribute(Attributes.VIEW, new CreateDetailsView(availabilityView, nights));
        model.addAttribute(Attributes.FORM, new OfflineBookingForm(
                checkInAt, checkOutAt, null, null, null, null, null, roomSelections));
        return "receptionist/reservation/details";
    }

    @PostMapping("/create/details")
    String createBooking(@Valid @ModelAttribute(Attributes.FORM) OfflineBookingForm form,
                         BindingResult bindingResult, RedirectAttributes redirect, Model model) {
        offlineBookingFormValidator.validate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(form.checkInAt(), form.checkOutAt());
            long nights = ChronoUnit.DAYS.between(form.checkInAt(), form.checkOutAt());
            model.addAttribute(Attributes.VIEW, new CreateDetailsView(availabilityView, nights));
            return "receptionist/reservation/details";
        }

        reservationService.createReservation(form);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Offline booking created successfully.");
        return "redirect:/receptionist/bookings";
    }

    private List<RoomTypeAvailabilityView> getAvailabilityView(LocalDate checkInAt, LocalDate checkOutAt) {
        return roomAvailabilityService.getAvailability(checkInAt, checkOutAt).stream()
                .map(RoomTypeAvailabilityView::new).toList();
    }
}
