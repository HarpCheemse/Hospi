package com.hospi.manage.features.admin.config.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.admin.config.dto.SystemConfigForm;
import com.hospi.manage.features.admin.config.entity.SystemConfig;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemConfigService systemConfigService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void config_shouldRender() throws Exception {
        SystemConfig config = new SystemConfig();
        config.setHotelId(1L);
        config.setTaxRate(new BigDecimal("10.00"));
        config.setDefaultDepositPercentage(new BigDecimal("20.00"));
        config.setLateCheckoutFee(new BigDecimal("50.00"));
        config.setExtraGuestFee(new BigDecimal("15.00"));
        config.setPendingBookingExpiryMinutes(30);
        config.setCancellationHoursBeforeCheckin(24);
        config.setMaximumBookingDays(30);
        config.setMaximumRoomPerBook(5);
        config.setRefundPercentage(new BigDecimal("50.00"));
        config.setFullRefundWindowHours(24);
        when(systemConfigService.getConfig()).thenReturn(config);

        mockMvc.perform(get("/admin/configs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/config/list"))
                .andExpect(model().attributeExists("configs"))
                .andExpect(content().string(containsString("10.00")));
    }

    @Test
    void edit_shouldRender() throws Exception {
        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("50.00"),
                new BigDecimal("15.00"), 30, 24, 30, 5,
                new BigDecimal("50.00"), 24);
        when(systemConfigService.getForm()).thenReturn(form);

        mockMvc.perform(get("/admin/configs/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/config/edit"))
                .andExpect(model().attribute(Attributes.FORM, form));
    }

    @Test
    void updateConfigs_shouldRedirect_onSuccess() throws Exception {
        mockMvc.perform(post("/admin/configs/edit")
                        .param("taxRate", "10.00")
                        .param("defaultDepositPercentage", "20.00")
                        .param("lateCheckoutFee", "50.00")
                        .param("extraGuestFee", "15.00")
                        .param("pendingBookingExpiryMinutes", "30")
                        .param("cancellationHoursBeforeCheckin", "24")
                        .param("maximumBookingDays", "30")
                        .param("maximumRoomPerBook", "5")
                        .param("refundPercentage", "50.00")
                        .param("fullRefundWindowHours", "24"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/configs"))
                .andExpect(flash().attribute(Attributes.SUCCESS, "Configs updated successfully"));
    }

    @Test
    void updateConfigs_shouldReRender_onValidationError() throws Exception {
        mockMvc.perform(post("/admin/configs/edit")
                        .param("taxRate", "")
                        .param("defaultDepositPercentage", "")
                        .param("lateCheckoutFee", "")
                        .param("extraGuestFee", "")
                        .param("pendingBookingExpiryMinutes", "")
                        .param("cancellationHoursBeforeCheckin", "")
                        .param("maximumBookingDays", "")
                        .param("maximumRoomPerBook", "")
                        .param("refundPercentage", "")
                        .param("fullRefundWindowHours", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/config/edit"));
    }
}
