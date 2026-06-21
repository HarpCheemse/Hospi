package com.hospi.manage.features.invoice.entity;

import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "deposit_used")
    private BigDecimal depositUsed;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "balance_due")
    private BigDecimal balanceDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private InvoiceStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
