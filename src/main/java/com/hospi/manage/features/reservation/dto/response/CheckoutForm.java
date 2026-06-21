package com.hospi.manage.features.receptionist.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CheckoutForm {

    private boolean applyLateFee;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime actualCheckoutTime;

    private String paymentMethod;

    public boolean isApplyLateFee() { return applyLateFee; }
    public void setApplyLateFee(boolean v) { this.applyLateFee = v; }
    public LocalDateTime getActualCheckoutTime() { return actualCheckoutTime; }
    public void setActualCheckoutTime(LocalDateTime v) { this.actualCheckoutTime = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
}
