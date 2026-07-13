package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.reservation.dto.request.AssignRoomForm;
import com.hospi.manage.features.reservation.dto.request.CheckoutForm;
import com.hospi.manage.features.reservation.dto.request.ExtendStayForm;
import com.hospi.manage.features.reservation.dto.request.StayingGuestForm;
import com.hospi.manage.features.reservation.dto.response.CheckoutCalculation;
import com.hospi.manage.features.reservation.dto.response.ManageReservationView;
import com.hospi.manage.features.reservation.dto.response.ReservationSummaryView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.mapper.ReservationMapper;
import com.hospi.manage.features.reservation.service.CheckoutService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.reservation.service.RoomUpgradeService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Controller for the receptionist current stays management (manage, guests, rooms, extend, swap). */
@Controller
@RequestMapping("/receptionist/stays")
@RequiredArgsConstructor
public class ReceptionistStayController {

    private final ReservationService reservationService;
    private final StayingGuestService stayingGuestService;
    private final RoomAssignmentService roomAssignmentService;
    private final RoomUpgradeService roomUpgradeService;
    private final CheckoutService checkoutService;
    private final SystemConfigService systemConfigService;
    private final AuditService auditService;

    private static final int PAGE_SIZE = 10;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, Attributes.CURRENT_STAYS);
    }

    @GetMapping
    String currentStays(@RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        model.addAttribute(Attributes.VIEW,
                ReservationMapper.toCurrentStaysView(reservationService.findCheckedInFiltered(
                        search, PageRequest.of(page, PAGE_SIZE)), search));
        return "receptionist/reservation/stays";
    }

    @GetMapping("/{id}")
    String manage(@PathVariable Long id, @RequestParam(required = false) Long edit, Model model) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            return "redirect:/receptionist/stays";
        }

        model.addAttribute(Attributes.VIEW, buildManageView(id, reservation));

        if (edit != null) {
            var guest = stayingGuestService.getGuest(id, edit);
            model.addAttribute(Attributes.FORM, new StayingGuestForm(
                    guest.getGuestName(), guest.getDateOfBirth(), guest.getNationality()));
            model.addAttribute(Attributes.EDIT_GUEST_ID, edit);
        } else {
            model.addAttribute(Attributes.FORM, new StayingGuestForm(null, null, null));
        }

        addCheckoutAttributes(id, reservation, model);
        model.addAttribute(Attributes.ROOM_FORM, new AssignRoomForm(null));
        model.addAttribute(Attributes.EXTEND_FORM, new ExtendStayForm(null));
        return "receptionist/reservation/manage";
    }

    @PostMapping("/{id}/manage/guests")
    String addGuest(@PathVariable Long id, @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form,
                    BindingResult binding, Model model, RedirectAttributes redirect,
                    @AuthenticationPrincipal AccountPrincipal principal) {
        if (binding.hasErrors()) {
            var r = reservationService.findById(id);
            model.addAttribute(Attributes.VIEW, buildManageView(id, r));
            addCheckoutAttributes(id, r, model);
            model.addAttribute(Attributes.ROOM_FORM, new AssignRoomForm(null));
            model.addAttribute(Attributes.EXTEND_FORM, new ExtendStayForm(null));
            return "receptionist/reservation/manage";
        }
        stayingGuestService.addGuest(id, form);
        auditService.log(principal.getId(), principal.getUsername(), "CREATE", "STAYING_GUEST", id,
                "Guest added: " + form.guestName());
        redirect.addFlashAttribute(Attributes.SUCCESS, "Guest added successfully.");
        return "redirect:/receptionist/stays/" + id + "";
    }

    @PostMapping("/{id}/manage/guests/{guestId}")
    String editGuest(@PathVariable Long id, @PathVariable Long guestId,
                     @Valid @ModelAttribute(Attributes.FORM) StayingGuestForm form,
                     BindingResult binding, Model model, RedirectAttributes redirect,
                     @AuthenticationPrincipal AccountPrincipal principal) {
        if (binding.hasErrors()) {
            var r = reservationService.findById(id);
            model.addAttribute(Attributes.VIEW, buildManageView(id, r));
            addCheckoutAttributes(id, r, model);
            model.addAttribute(Attributes.EDIT_GUEST_ID, guestId);
            model.addAttribute(Attributes.ROOM_FORM, new AssignRoomForm(null));
            model.addAttribute(Attributes.EXTEND_FORM, new ExtendStayForm(null));
            return "receptionist/reservation/manage";
        }
        stayingGuestService.updateGuest(id, guestId, form);
        auditService.log(principal.getId(), principal.getUsername(), "UPDATE", "STAYING_GUEST", guestId,
                "Guest updated: " + form.guestName());
        redirect.addFlashAttribute(Attributes.SUCCESS, "Guest updated successfully.");
        return "redirect:/receptionist/stays/" + id + "";
    }

    @PostMapping("/{id}/manage/guests/{guestId}/delete")
    String deleteGuest(@PathVariable Long id, @PathVariable Long guestId, RedirectAttributes redirect,
                       @AuthenticationPrincipal AccountPrincipal principal) {
        stayingGuestService.deleteGuest(id, guestId);
        auditService.log(principal.getId(), principal.getUsername(), "DELETE", "STAYING_GUEST", guestId,
                "Guest removed from reservation " + id);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Guest removed successfully.");
        return "redirect:/receptionist/stays/" + id + "";
    }

    @PostMapping("/{id}/manage/rooms")
    String assignRoom(@PathVariable Long id, @Valid @ModelAttribute(Attributes.ROOM_FORM) AssignRoomForm form,
                      BindingResult binding, Model model, RedirectAttributes redirect,
                      @AuthenticationPrincipal AccountPrincipal principal) {
        if (binding.hasErrors()) {
            var r = reservationService.findById(id);
            model.addAttribute(Attributes.VIEW, buildManageView(id, r));
            addCheckoutAttributes(id, r, model);
            model.addAttribute(Attributes.FORM, new StayingGuestForm(null, null, null));
            model.addAttribute(Attributes.EXTEND_FORM, new ExtendStayForm(null));
            return "receptionist/reservation/manage";
        }
        try {
            roomAssignmentService.assignRoom(id, form.roomId());
            auditService.log(principal.getId(), principal.getUsername(), "ASSIGN_ROOM", "RESERVATION", id,
                    "Room " + form.roomId() + " assigned");
            redirect.addFlashAttribute(Attributes.SUCCESS, "Room assigned successfully.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
        }
        return "redirect:/receptionist/stays/" + id + "";
    }

    @PostMapping("/{id}/manage/rooms/{assignmentId}/remove")
    String removeRoom(@PathVariable Long id, @PathVariable Long assignmentId, RedirectAttributes redirect,
                      @AuthenticationPrincipal AccountPrincipal principal) {
        roomAssignmentService.removeAssignment(id, assignmentId);
        auditService.log(principal.getId(), principal.getUsername(), "REMOVE_ROOM", "RESERVATION", id,
                "Room assignment " + assignmentId + " removed");
        redirect.addFlashAttribute(Attributes.SUCCESS, "Room assignment removed successfully.");
        return "redirect:/receptionist/stays/" + id + "";
    }

    @PostMapping("/{id}/manage/extend")
    String extendStay(@PathVariable Long id, @Valid @ModelAttribute(Attributes.EXTEND_FORM) ExtendStayForm form,
                      BindingResult binding, Model model, RedirectAttributes redirect,
                      @AuthenticationPrincipal AccountPrincipal principal) {
        if (binding.hasErrors()) {
            var r = reservationService.findById(id);
            model.addAttribute(Attributes.VIEW, buildManageView(id, r));
            addCheckoutAttributes(id, r, model);
            model.addAttribute(Attributes.FORM, new StayingGuestForm(null, null, null));
            model.addAttribute(Attributes.ROOM_FORM, new AssignRoomForm(null));
            return "receptionist/reservation/manage";
        }
        try {
            reservationService.extendStay(id, form.extraDays());
            auditService.log(principal.getId(), principal.getUsername(), "EXTEND_STAY", "RESERVATION", id,
                    "Extended by " + form.extraDays() + " days");
            redirect.addFlashAttribute(Attributes.SUCCESS, "Stay extended successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
        }
        return "redirect:/receptionist/stays/" + id + "";
    }

    @GetMapping("/{id}/swap")
    String swapForm(@PathVariable Long id, @RequestParam Long from, Model model) {
        var reservation = reservationService.findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            return "redirect:/receptionist/stays";
        }
        model.addAttribute(Attributes.RESERVATION, ReservationSummaryView.from(reservation));
        model.addAttribute(Attributes.FROM_TYPE_ID, from);

        var detail = reservation.getDetails().stream()
                .filter(d -> d.getRoomType().getId().equals(from))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Room type"));

        if (reservation.getCheckOutAt() != null) {
            var remainingNights = ChronoUnit.DAYS.between(LocalDate.now(), reservation.getCheckOutAt());
            model.addAttribute(Attributes.OPTIONS, roomUpgradeService.getUpgradeOptions(reservation, from));
            model.addAttribute(Attributes.CURRENT_TYPE, detail.getRoomType());
            model.addAttribute(Attributes.REMAINING_NIGHTS, remainingNights);
            model.addAttribute(Attributes.TOTAL_NIGHTS, ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt()));
        }
        return "receptionist/reservation/swap";
    }

    @PostMapping("/{id}/swap")
    String swapRoom(@PathVariable Long id, @RequestParam Long roomTypeId,
                    @RequestParam Long from, RedirectAttributes redirect,
                    @AuthenticationPrincipal AccountPrincipal principal) {
        try {
            roomUpgradeService.swapOne(id, from, roomTypeId);
            auditService.log(principal.getId(), principal.getUsername(), "SWAP_ROOM", "RESERVATION", id,
                    "Swapped from type " + from + " to " + roomTypeId);
            redirect.addFlashAttribute(Attributes.SUCCESS, "Room swapped successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
        }
        return "redirect:/receptionist/stays/" + id + "";
    }

    @PostMapping("/{id}/checkout")
    String checkout(@PathVariable Long id, @ModelAttribute CheckoutForm form,
                    @AuthenticationPrincipal AccountPrincipal principal, RedirectAttributes redirect) {
        try {
            var reservation = reservationService.findById(id);
            int adultGuests = stayingGuestService.getAdultGuestCount(id, reservation.getCheckInAt());
            boolean applyLateFee = Boolean.TRUE.equals(form.applyLateFee());
            LocalDateTime actualTime = applyLateFee ? form.actualCheckoutTime() : null;
            CheckoutCalculation calc = checkoutService.calculate(reservation, actualTime, adultGuests);
            PaymentMethod method = PaymentMethod.valueOf(form.paymentMethod());
            checkoutService.complete(id, calc, method, principal.getUsername(), form.actualCheckoutTime(), applyLateFee);
            auditService.log(principal.getId(), principal.getUsername(), "CHECKOUT", "RESERVATION", id,
                    "Amount: $" + calc.totalDue());
            redirect.addFlashAttribute(Attributes.SUCCESS, "Checkout completed successfully.");
            return "redirect:/receptionist/stays";
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
            return "redirect:/receptionist/stays/" + id + "";
        }
    }

    private ManageReservationView buildManageView(Long id, Reservation reservation) {
        return ReservationMapper.toManageView(reservation,
                stayingGuestService.getGuests(id),
                roomAssignmentService.getAssignedRooms(id),
                roomAssignmentService.getAvailableRooms(id));
    }

    private void addCheckoutAttributes(Long id, Reservation reservation, Model model) {
        int adultGuests = stayingGuestService.getAdultGuestCount(id, reservation.getCheckInAt());
        model.addAttribute("checkoutCalc", checkoutService.calculate(reservation, null, adultGuests));
        model.addAttribute("config", systemConfigService.getConfig());
    }
}
