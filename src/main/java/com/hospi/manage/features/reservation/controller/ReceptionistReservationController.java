package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.dto.request.*;
import com.hospi.manage.features.reservation.dto.response.*;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.mapper.ReservationMapper;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import com.hospi.manage.features.reservation.validation.DateSearchValidator;
import com.hospi.manage.features.reservation.validation.OfflineBookingFormValidator;
import com.hospi.manage.features.reservation.service.RoomUpgradeService;
import com.hospi.manage.features.room.dto.response.RoomSelection;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for receptionist reservation management (listing, creating,
 * managing stays, guest and room assignment).
 */
@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class ReceptionistReservationController {

    private final ReservationService reservationService;
    private final OfflineBookingFormValidator offlineBookingFormValidator;
    private final DateSearchValidator dateSearchValidator;
    private final RoomAvailabilityService roomAvailabilityService;
    private final StayingGuestService stayingGuestService;
    private final RoomAssignmentService roomAssignmentService;
    private final RoomUpgradeService roomUpgradeService;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    private final static int pageSize = 10;

    /**
     * Show the active bookings list with optional filtering by status, date, and
     * search term. Paginated with a default page size of 10.
     */
    @GetMapping
    String activeBookings(@RequestParam(required = false) String status,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          @RequestParam(required = false) String search,
                          @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        List<ReservationStatus> statuses;
        if (status != null && !status.isBlank()) {
            statuses = List.of(ReservationStatus.valueOf(status));
        } else {
            statuses = List.of(ReservationStatus.PENDING,
                    ReservationStatus.CONFIRMED);
        }

        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                Attributes.ACTIVE_BOOKINGS);
        model.addAttribute(Attributes.VIEW,
                ReservationMapper.toActiveBookingsView(reservationService.findFiltered(statuses,
                                search,
                                date,
                                PageRequest.of(page,
                                        pageSize)),
                        status,
                        date,
                        search));
        return "receptionist/reservation/active";
    }

    /**
     * Show the current stays (checked-in reservations) list with optional search.
     * Paginated.
     */
    @GetMapping("/stays")
    String currentStays(@RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                Attributes.CURRENT_STAYS);
        model.addAttribute(Attributes.VIEW,
                ReservationMapper.toCurrentStaysView(reservationService.findCheckedInFiltered(search,
                                PageRequest.of(page,
                                        pageSize)),
                        search));
        return "receptionist/reservation/stays";
    }

    /**
     * Show the date selection form for creating a new offline booking.
     */
    @GetMapping("/create")
    String create(Model model) {
        model.addAttribute(Attributes.FORM,
                new DateSearchForm(null,
                        null));
        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                Attributes.ACTIVE_BOOKINGS);
        return "receptionist/reservation/create";
    }

    /**
     * Validate the date search and redirect to the details page with check-in and
     * check-out dates as query parameters.
     */
    @PostMapping("/create")
    String searchDates(@Valid @ModelAttribute(Attributes.FORM) DateSearchForm form, BindingResult binding,
                       Model model) {
        dateSearchValidator.validate(form,
                binding);

        if (binding.hasErrors()) {
            return "receptionist/reservation/create";
        }

        return "redirect:" + UriComponentsBuilder.fromPath("/receptionist/reservations/create/details").queryParam("checkInAt",
                form.checkInAt()).queryParam("checkOutAt",
                form.checkOutAt()).build().toUriString();
    }

    /**
     * Show the booking details form with room availability and selection for the
     * given dates.
     */
    @GetMapping("/create/details")
    String createDetails(@RequestParam(required = false) LocalDate checkInAt,
                         @RequestParam(required = false) LocalDate checkOutAt, Model model) {
        if (checkInAt == null || checkOutAt == null) {
            return "redirect:/receptionist/reservations/create";
        }

        List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(checkInAt,
                checkOutAt);

        long nights = ChronoUnit.DAYS.between(checkInAt,
                checkOutAt);

        List<RoomSelection> roomSelections = availabilityView.stream().map(rt -> new RoomSelection(rt.roomTypeId(),
                0)).toList();

        OfflineBookingForm form = new OfflineBookingForm(checkInAt,
                checkOutAt,
                null,
                null,
                null,
                null,
                null,
                roomSelections);

        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                Attributes.ACTIVE_BOOKINGS);
        model.addAttribute(Attributes.VIEW,
                new CreateDetailsView(availabilityView,
                        nights));
        model.addAttribute(Attributes.FORM,
                form);

        return "receptionist/reservation/details";
    }

    /**
     * Create an offline booking from the details form. Validates, persists the
     * reservation, and redirects to the active bookings list.
     */
    @PostMapping("/create/details")
    String createBooking(@Valid @ModelAttribute(Attributes.FORM) OfflineBookingForm form, BindingResult bindingResult,
                         RedirectAttributes redirect, Model model) {

        offlineBookingFormValidator.validate(form,
                bindingResult);

        if (bindingResult.hasErrors()) {
            List<RoomTypeAvailabilityView> availabilityView = getAvailabilityView(form.checkInAt(),
                    form.checkOutAt());
            long nights = ChronoUnit.DAYS.between(form.checkInAt(),
                    form.checkOutAt());
            model.addAttribute(Attributes.VIEW,
                    new CreateDetailsView(availabilityView,
                            nights));
            model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                    Attributes.ACTIVE_BOOKINGS);

            return "receptionist/reservation/details";
        }

        reservationService.createReservation(form);

        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Offline booking created successfully.");

        return "redirect:/receptionist/reservations";
    }

    /**
     * Show the manage-stay page for a checked-in reservation. Supports an optional
     * {@code edit} parameter to pre-populate the guest edit form for a specific
     * guest.
     */
    @GetMapping("/{id}/manage")
    String manage(@PathVariable Long id, @RequestParam(required = false) Long edit, Model model) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            return "redirect:/receptionist/reservations/stays";
        }

        model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                Attributes.CURRENT_STAYS);
        model.addAttribute(Attributes.VIEW,
                buildManageView(id,
                        reservation));

        if (edit != null) {
            var guest = stayingGuestService.getGuest(id,
                    edit);
            model.addAttribute(Attributes.FORM,
                    new StayingGuestForm(guest.getGuestName(),
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

        model.addAttribute("roomForm",
                new AssignRoomForm(null));
        model.addAttribute("extendForm",
                new ExtendStayForm(null));

        return "receptionist/reservation/manage";
    }

    /**
     * Add a guest to a checked-in reservation. Re-renders the manage page on
     * validation failure; redirects on success.
     */
    @PostMapping("/{id}/manage/guests")
    String addGuest(@PathVariable Long id, @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form,
                    BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute(Attributes.VIEW,
                    buildManageView(id,
                            reservationService.findById(id)));
            model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                    Attributes.CURRENT_STAYS);
            model.addAttribute("roomForm",
                    new AssignRoomForm(null));
            model.addAttribute("extendForm",
                    new ExtendStayForm(null));
            return "receptionist/reservation/manage";
        }
        stayingGuestService.addGuest(id,
                form);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Guest added successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    /**
     * Update a guest's details in a checked-in reservation. Re-renders the manage
     * page on validation failure; redirects on success.
     */
    @PostMapping("/{id}/manage/guests/{guestId}")
    String editGuest(@PathVariable Long id, @PathVariable Long guestId,
                     @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form, BindingResult binding, Model model,
                     RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute(Attributes.VIEW,
                    buildManageView(id,
                            reservationService.findById(id)));
            model.addAttribute("editGuestId",
                    guestId);
            model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                    Attributes.CURRENT_STAYS);
            model.addAttribute("roomForm",
                    new AssignRoomForm(null));
            model.addAttribute("extendForm",
                    new ExtendStayForm(null));
            return "receptionist/reservation/manage";
        }
        stayingGuestService.updateGuest(id,
                guestId,
                form);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Guest updated successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    /**
     * Remove a guest from a checked-in reservation. Redirects to the manage page
     * on success.
     */
    @PostMapping("/{id}/manage/guests/{guestId}/delete")
    String deleteGuest(@PathVariable Long id, @PathVariable Long guestId, RedirectAttributes redirect) {
        stayingGuestService.deleteGuest(id,
                guestId);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Guest removed successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    /**
     * Assign a room to a checked-in reservation. Redirects to the manage page with
     * success or error flash.
     */
    @PostMapping("/{id}/manage/rooms")
    String assignRoom(@PathVariable Long id, @Valid @ModelAttribute("roomForm") AssignRoomForm form,
                      BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute(Attributes.VIEW,
                    buildManageView(id,
                            reservationService.findById(id)));
            model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                    Attributes.CURRENT_STAYS);
            model.addAttribute(Attributes.FORM,
                    new StayingGuestForm(null, null, null));
            model.addAttribute("extendForm",
                    new ExtendStayForm(null));
            return "receptionist/reservation/manage";
        }
        try {
            roomAssignmentService.assignRoom(id,
                    form.roomId());
            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Room assigned successfully.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    /**
     * Remove a room assignment from a checked-in reservation. Redirects to the
     * manage page on success.
     */
    @PostMapping("/{id}/manage/rooms/{assignmentId}/remove")
    String removeRoom(@PathVariable Long id, @PathVariable Long assignmentId, RedirectAttributes redirect) {
        roomAssignmentService.removeAssignment(id,
                assignmentId);
        redirect.addFlashAttribute(Attributes.SUCCESS,
                "Room assignment removed successfully.");
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    private ManageReservationView buildManageView(Long id, Reservation reservation) {
        return ReservationMapper.toManageView(reservation,
                stayingGuestService.getGuests(id),
                roomAssignmentService.getAssignedRooms(id),
                roomAssignmentService.getAvailableRooms(id));
    }

    private List<RoomTypeAvailabilityView> getAvailabilityView(LocalDate checkInAt, LocalDate checkOutAt) {
        return roomAvailabilityService.getAvailability(checkInAt,
                checkOutAt).stream().map(RoomTypeAvailabilityView::new).toList();
    }

    /**
     * Extend a checked-in reservation by the specified number of extra days.
     * Redirects to the manage page with success or error flash.
     */
    @PostMapping("/{id}/manage/extend")
    public String extendStay(@PathVariable Long id, @Valid @ModelAttribute("extendForm") ExtendStayForm form,
                             BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute(Attributes.VIEW,
                    buildManageView(id,
                            reservationService.findById(id)));
            model.addAttribute(Attributes.ACTIVE_SIDEBAR,
                    Attributes.CURRENT_STAYS);
            model.addAttribute(Attributes.FORM,
                    new StayingGuestForm(null, null, null));
            model.addAttribute("roomForm",
                    new AssignRoomForm(null));
            return "receptionist/reservation/manage";
        }

        try {
            reservationService.extendStay(id,
                    form.extraDays());

            redirect.addFlashAttribute(Attributes.SUCCESS,
                    "Stay extended successfully.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR,
                    e.getMessage());
        }

        return "redirect:/receptionist/reservations/" + id + "/manage";
    }

    /** Show available room types to swap to, for a specific current room type. */
    @GetMapping("/{id}/manage/swap")
    String swapForm(@PathVariable Long id, @RequestParam Long from, Model model) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            return "redirect:/receptionist/reservations/stays";
        }
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, Attributes.CURRENT_STAYS);
        model.addAttribute("reservation", reservation);
        model.addAttribute("fromTypeId", from);

        var detail = reservation.getDetails().stream()
                .filter(d -> d.getRoomType().getId().equals(from))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Room type"));

        if (reservation.getCheckOutAt() != null) {
            var remainingNights = ChronoUnit.DAYS.between(LocalDate.now(), reservation.getCheckOutAt());
            var allTypes = roomTypeRepository.findByActiveTrue();

            List<RoomType> options = new ArrayList<>();
            for (var rt : allTypes) {
                if (rt.getBasePrice().compareTo(detail.getBasePrice()) <= 0) continue;
                if (rt.getId().equals(from)) continue;
                boolean hasVacant = !roomRepository
                        .findByRoomTypeIdAndOccupancyStatusAndActiveTrue(rt.getId(), OccupancyStatus.VACANT)
                        .isEmpty();
                if (!hasVacant) continue;
                options.add(rt);
            }
            model.addAttribute("options", options);
            model.addAttribute("currentType", detail.getRoomType());
            model.addAttribute("remainingNights", remainingNights);
            model.addAttribute("totalNights", ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt()));
        }

        return "receptionist/reservation/swap";
    }

    /** Swap one unit from a room type to a higher-tier type. */
    @PostMapping("/{id}/manage/swap")
    String swapRoom(@PathVariable Long id, @RequestParam Long roomTypeId,
                    @RequestParam Long from, RedirectAttributes redirect) {
        try {
            roomUpgradeService.swapOne(id, from, roomTypeId);
            redirect.addFlashAttribute(Attributes.SUCCESS, "Room swapped successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
        }
        return "redirect:/receptionist/reservations/" + id + "/manage";
    }
}
