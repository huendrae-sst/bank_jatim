package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "switching_stock_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchingStockItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "switching_stock_id", nullable = false)
    private SwitchingStock switchingStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty_requested", nullable = false)
    @Builder.Default
    private Integer qtyRequested = 1;

    @Column(name = "qty_approved", nullable = false)
    @Builder.Default
    private Integer qtyApproved = 0;

    @Column(name = "qty_transferred", nullable = false)
    @Builder.Default
    private Integer qtyTransferred = 0;

    @Column(name = "qty_received", nullable = false)
    @Builder.Default
    private Integer qtyReceived = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
