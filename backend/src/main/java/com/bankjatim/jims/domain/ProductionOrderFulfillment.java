package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_order_fulfillments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"production_order_id", "order_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderFulfillment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "qty_fulfilled", nullable = false)
    @Builder.Default
    private Integer qtyFulfilled = 0;

    @Column(name = "fulfilled_at")
    @Builder.Default
    private LocalDateTime fulfilledAt = LocalDateTime.now();
}
