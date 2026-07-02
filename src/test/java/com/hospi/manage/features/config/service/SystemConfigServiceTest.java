package com.hospi.manage.features.config.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.config.dto.SystemConfigForm;
import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.repository.SystemConfigRepository;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private NotificationService notificationService;

    private SystemConfigService systemConfigService;

    @BeforeEach
    void setUp() {
        systemConfigService = new SystemConfigService(systemConfigRepository, notificationService);
    }

    @Test
    void getConfig_shouldReturnConfig_whenFound() {
        SystemConfig config = new SystemConfig();
        config.setHotelId(HotelConstants.HOTEL_ID);
        when(systemConfigRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(config));

        SystemConfig result = systemConfigService.getConfig();

        assertNotNull(result);
        assertEquals(HotelConstants.HOTEL_ID, result.getHotelId());
    }

    @Test
    void getConfig_shouldThrow_whenNotFound() {
        when(systemConfigRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> systemConfigService.getConfig());
    }

    @Test
    void getForm_shouldReturnPopulatedForm() {
        SystemConfig config = new SystemConfig();
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
        when(systemConfigRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(config));

        SystemConfigForm form = systemConfigService.getForm();

        assertEquals(new BigDecimal("10.00"), form.taxRate());
        assertEquals(new BigDecimal("20.00"), form.defaultDepositPercentage());
        assertEquals(new BigDecimal("50.00"), form.lateCheckoutFee());
        assertEquals(new BigDecimal("15.00"), form.extraGuestFee());
        assertEquals(30, form.pendingBookingExpiryMinutes());
        assertEquals(24, form.cancellationHoursBeforeCheckin());
        assertEquals(30, form.maximumBookingDays());
        assertEquals(5, form.maximumRoomPerBook());
        assertEquals(new BigDecimal("50.00"), form.refundPercentage());
        assertEquals(24, form.fullRefundWindowHours());
    }

    @Test
    void updateSystemConfigs_shouldSetFieldsAndSave() {
        SystemConfig config = new SystemConfig();
        config.setHotelId(HotelConstants.HOTEL_ID);
        when(systemConfigRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(config));

        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("8.00"), new BigDecimal("25.00"), new BigDecimal("60.00"),
                new BigDecimal("20.00"), 45, 12, 60, 10,
                new BigDecimal("75.00"), 48);

        systemConfigService.updateSystemConfigs(form);

        assertEquals(new BigDecimal("8.00"), config.getTaxRate());
        assertEquals(new BigDecimal("25.00"), config.getDefaultDepositPercentage());
        assertEquals(new BigDecimal("60.00"), config.getLateCheckoutFee());
        assertEquals(new BigDecimal("20.00"), config.getExtraGuestFee());
        assertEquals(45, config.getPendingBookingExpiryMinutes());
        assertEquals(12, config.getCancellationHoursBeforeCheckin());
        assertEquals(60, config.getMaximumBookingDays());
        assertEquals(10, config.getMaximumRoomPerBook());
        assertEquals(new BigDecimal("75.00"), config.getRefundPercentage());
        assertEquals(48, config.getFullRefundWindowHours());
        verify(systemConfigRepository).save(config);
    }

    @Test
    void updateSystemConfigs_shouldNotifyAdmins() {
        SystemConfig config = new SystemConfig();
        config.setHotelId(HotelConstants.HOTEL_ID);
        when(systemConfigRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.of(config));

        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("8.00"), new BigDecimal("25.00"), new BigDecimal("60.00"),
                new BigDecimal("20.00"), 45, 12, 60, 10,
                new BigDecimal("75.00"), 48);

        systemConfigService.updateSystemConfigs(form);

        verify(notificationService).notifyRole(Role.ADMIN, "System Configuration Updated",
                "Hotel system settings have been modified", "CONFIG", "system");
    }

    @Test
    void updateSystemConfigs_shouldThrow_whenConfigNotFound() {
        when(systemConfigRepository.findById(HotelConstants.HOTEL_ID)).thenReturn(Optional.empty());

        SystemConfigForm form = new SystemConfigForm(
                new BigDecimal("8.00"), new BigDecimal("25.00"), new BigDecimal("60.00"),
                new BigDecimal("20.00"), 45, 12, 60, 10,
                new BigDecimal("75.00"), 48);

        assertThrows(ResourceNotFoundException.class, () -> systemConfigService.updateSystemConfigs(form));
    }
}
