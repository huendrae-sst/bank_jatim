package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_ledgers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLedger extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "transaction_type", nullable = false, length = 100)
    private String transactionType;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "qty_in", nullable = false)
    @Builder.Default
    private Integer qtyIn = 0;

    @Column(name = "qty_out", nullable = false)
    @Builder.Default
    private Integer qtyOut = 0;

    @Column(name = "balance_after", nullable = false)
    @Builder.Default
    private Integer balanceAfter = 0;

    @Column(name = "unit_cost", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "total_value", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;
}
