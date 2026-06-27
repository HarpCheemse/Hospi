package com.hospi.manage.features.config.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.config.dto.SystemConfigForm;
import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.repository.SystemConfigRepository;
import com.hospi.manage.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Business logic for reading and updating system-wide hotel configuration. */
@Service
@RequiredArgsConstructor
public class SystemConfigService {
    private final SystemConfigRepository systemConfigRepository;
    private final NotificationService notificationService;

    /** Retrieve the system configuration for the hotel. */
    public SystemConfig getConfig() {
        return systemConfigRepository.findById(HotelConstants.HOTEL_ID).
                orElseThrow(() -> new ResourceNotFoundException("System configs"));
    }

    /** Return the system configuration form populated with current values. */
    public SystemConfigForm getForm() {
        SystemConfig configs = getConfig();
        SystemConfigForm form = new SystemConfigForm(
                configs.getTaxRate(),
                configs.getDefaultDepositPercentage(),
                configs.getLateCheckoutFee(),
                configs.getExtraGuestFee(),
                configs.getPendingBookingExpiryMinutes(),
                configs.getCancellationHoursBeforeCheckin(),
                configs.getMaximumBookingDays(),
                configs.getMaximumRoomPerBook(),
                configs.getRefundPercentage(),
                configs.getFullRefundWindowHours()
        );
        return form;
    }

    /** Update all system configuration fields. */
    public void updateSystemConfigs(SystemConfigForm form) {
        SystemConfig configs = getConfig();

        configs.setExtraGuestFee(form.extraGuestFee());
        configs.setDefaultDepositPercentage(form.defaultDepositPercentage());
        configs.setCancellationHoursBeforeCheckin(form.cancellationHoursBeforeCheckin());
        configs.setTaxRate(form.taxRate());
        configs.setLateCheckoutFee(form.lateCheckoutFee());
        configs.setMaximumRoomPerBook(form.maximumRoomPerBook());
        configs.setMaximumBookingDays(form.maximumBookingDays());
        configs.setPendingBookingExpiryMinutes(form.pendingBookingExpiryMinutes());
        configs.setRefundPercentage(form.refundPercentage());
        configs.setFullRefundWindowHours(form.fullRefundWindowHours());

        systemConfigRepository.save(configs);

        // Notify admins of config change
        notificationService.notifyRole(
                Role.ADMIN,
                "System Configuration Updated",
                "Hotel system settings have been modified",
                "CONFIG",
                "system"
        );
    }
}
