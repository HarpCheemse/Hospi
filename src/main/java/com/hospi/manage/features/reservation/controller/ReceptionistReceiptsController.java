package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.reservation.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/** Controller for receptionist today's receipts list. */
@Controller
@RequestMapping("/receptionist/receipts")
@RequiredArgsConstructor
public class ReceptionistReceiptsController {

    private final ReceiptService receiptService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "RECEIPTS");
    }

    @GetMapping
    String listToday(Model model) {
        model.addAttribute(Attributes.VIEW, receiptService.getTodayReceipts());
        return "receptionist/reservation/receipt";
    }
}
