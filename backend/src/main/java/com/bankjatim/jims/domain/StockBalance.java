package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_balances", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id", "item_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "on_hand", nullable = false)
    @Builder.Default
    private Integer onHand = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reserved = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer allocated = 0;

    @Column(name = "in_transit", nullable = false)
    @Builder.Default
    private Integer inTransit = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer hold = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer damaged = 0;

    @Column(name = "min_stock")
    private Integer minStockOverride;

    @Column(name = "max_stock")
    private Integer maxStockOverride;

    @Transient
    public int getAvailableStock() {
        return onHand - (reserved + allocated + hold + damaged);
    }
}
