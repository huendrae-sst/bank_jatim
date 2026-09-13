package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_allocations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_warehouse_id", nullable = false)
    private Warehouse sourceWarehouse;

    @Column(name = "qty_allocated", nullable = false)
    @Builder.Default
    private Integer qtyAllocated = 0;

    @Column(name = "allocation_type", nullable = false, length = 50)
    @Builder.Default
    private String allocationType = "DIRECT_WAREHOUSE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "switching_stock_id")
    private SwitchingStock switchingStock;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "RESERVED";
}
