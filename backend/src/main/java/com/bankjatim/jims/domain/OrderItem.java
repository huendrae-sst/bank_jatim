package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty_requested", nullable = false)
    @Builder.Default
    private Integer qtyRequested = 1;

    @Column(name = "qty_approved", nullable = false)
    @Builder.Default
    private Integer qtyApproved = 0;

    @Column(name = "qty_allocated", nullable = false)
    @Builder.Default
    private Integer qtyAllocated = 0;

    @Column(name = "qty_picked", nullable = false)
    @Builder.Default
    private Integer qtyPicked = 0;

    @Column(name = "qty_packed", nullable = false)
    @Builder.Default
    private Integer qtyPacked = 0;

    @Column(name = "qty_shipped", nullable = false)
    @Builder.Default
    private Integer qtyShipped = 0;

    @Column(name = "qty_received", nullable = false)
    @Builder.Default
    private Integer qtyReceived = 0;

    @Column(name = "unit_price_ref", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal unitPriceRef = BigDecimal.ZERO;

    @Column(name = "subtotal_ref", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subtotalRef = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
