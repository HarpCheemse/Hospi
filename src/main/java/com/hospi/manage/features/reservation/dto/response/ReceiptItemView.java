package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.invoice.entity.InvoiceItem;
import com.hospi.manage.features.invoice.enums.InvoiceItemType;

import java.math.BigDecimal;

public record ReceiptItemView(
        InvoiceItemType itemType,
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal amount
) {
    public static ReceiptItemView from(InvoiceItem item) {
        return new ReceiptItemView(
                item.getItemType(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount()
        );
    }
}
