package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.reservation.dto.response.ReceiptDetailView;
import com.hospi.manage.features.reservation.dto.response.ReceiptItemView;
import com.hospi.manage.features.reservation.dto.response.TodayReceiptView;
import com.hospi.manage.features.reservation.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceptionistReceiptsController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceptionistReceiptsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReceiptService receiptService;

    @Test
    void listToday_shouldRender() throws Exception {
        when(receiptService.getTodayReceipts()).thenReturn(List.of());

        mockMvc.perform(get("/receptionist/receipts"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/receipt"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void listToday_shouldShowReceipts() throws Exception {
        var receipt = new TodayReceiptView(
                1L, "John Doe", "Deluxe x1",
                BigDecimal.valueOf(500), BigDecimal.valueOf(500),
                LocalDateTime.now(), "receptionist");
        when(receiptService.getTodayReceipts()).thenReturn(List.of(receipt));

        mockMvc.perform(get("/receptionist/receipts"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/receipt"))
                .andExpect(model().attribute("view", List.of(receipt)));
    }

    @Test
    void detail_shouldRender() throws Exception {
        var detail = new ReceiptDetailView(
                1L, "John Doe", "101",
                BigDecimal.valueOf(500), BigDecimal.valueOf(40), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.valueOf(540), BigDecimal.valueOf(540),
                InvoiceStatus.PAID, List.of(), PaymentMethod.CASH,
                "receptionist", LocalDateTime.now(), LocalDateTime.now());
        when(receiptService.getReceiptDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/receptionist/receipts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/receipt-detail"))
                .andExpect(model().attribute("view", detail));
    }

    @Test
    void detail_shouldRender_withItems() throws Exception {
        var items = List.of(new ReceiptItemView(
                com.hospi.manage.features.invoice.enums.InvoiceItemType.ROOM,
                "Deluxe x2", 3, BigDecimal.valueOf(200), BigDecimal.valueOf(600)));
        var detail = new ReceiptDetailView(
                1L, "John Doe", "101, 102",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(80), BigDecimal.ZERO,
                BigDecimal.valueOf(25), BigDecimal.valueOf(1105), BigDecimal.valueOf(1105),
                InvoiceStatus.PAID, items, PaymentMethod.CARD,
                "receptionist", LocalDateTime.now(), LocalDateTime.now());
        when(receiptService.getReceiptDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/receptionist/receipts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/receipt-detail"))
                .andExpect(model().attribute("view", detail));
    }
}
