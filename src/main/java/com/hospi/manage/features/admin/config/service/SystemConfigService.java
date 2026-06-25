package com.hospi.manage.features.admin.config.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.admin.config.dto.SystemConfigForm;
import com.hospi.manage.features.admin.config.entity.SystemConfig;
import com.hospi.manage.features.admin.config.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

/** Business logic for reading and updating system-wide hotel configuration. */
@Service
public class SystemConfigService {
    private final SystemConfigRepository systemConfigRepository;

    SystemConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

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
    }
}
