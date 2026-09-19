package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "production_order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item; // Finished good (e.g. ATM-EMB-GPN-001)

    @Column(name = "qty_planned", nullable = false)
    @Builder.Default
    private Integer qtyPlanned = 0;

    @Column(name = "qty_produced", nullable = false)
    @Builder.Default
    private Integer qtyProduced = 0;

    @Column(name = "qty_damaged", nullable = false)
    @Builder.Default
    private Integer qtyDamaged = 0;
}
