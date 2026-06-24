package com.hospi.manage.features.receptionist.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckoutForm {

    private boolean applyLateFee;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime actualCheckoutTime;

    private BigDecimal extraGuestFeeOverride;

    private BigDecimal amountReceived;

    private String paymentMethod;

    public boolean isApplyLateFee() { return applyLateFee; }
    public void setApplyLateFee(boolean v) { this.applyLateFee = v; }
    public LocalDateTime getActualCheckoutTime() { return actualCheckoutTime; }
    public void setActualCheckoutTime(LocalDateTime v) { this.actualCheckoutTime = v; }
    public BigDecimal getExtraGuestFeeOverride() { return extraGuestFeeOverride; }
    public void setExtraGuestFeeOverride(BigDecimal v) { this.extraGuestFeeOverride = v; }
    public BigDecimal getAmountReceived() { return amountReceived; }
    public void setAmountReceived(BigDecimal v) { this.amountReceived = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
}
