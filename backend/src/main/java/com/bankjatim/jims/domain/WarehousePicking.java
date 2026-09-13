package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_pickings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehousePicking extends BaseEntity {

    @Column(name = "picking_number", nullable = false, unique = true, length = 100)
    private String pickingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picked_by_user_id", nullable = false)
    private User pickedByUser;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ASSIGNED"; // ASSIGNED, IN_PROGRESS, COMPLETED

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;
}
