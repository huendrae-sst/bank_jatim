package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_request_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id", nullable = false)
    private PurchaseRequest purchaseRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty_requested", nullable = false)
    @Builder.Default
    private Integer qtyRequested = 1;

    @Column(name = "qty_approved", nullable = false)
    @Builder.Default
    private Integer qtyApproved = 0;

    @Column(name = "qty_ordered", nullable = false)
    @Builder.Default
    private Integer qtyOrdered = 0;

    @Column(name = "estimated_unit_price", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal estimatedUnitPrice = BigDecimal.ZERO;

    @Column(name = "estimated_subtotal", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal estimatedSubtotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
