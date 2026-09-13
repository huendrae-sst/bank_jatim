package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_packings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehousePacking extends BaseEntity {

    @Column(name = "packing_number", nullable = false, unique = true, length = 100)
    private String packingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packed_by_user_id", nullable = false)
    private User packedByUser;

    @Column(name = "koli_count", nullable = false)
    @Builder.Default
    private Integer koliCount = 1;

    @Column(name = "total_weight_kg", precision = 8, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalWeightKg = new BigDecimal("1.00");

    @Column(name = "dimensions_cm", length = 50)
    private String dimensionsCm;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PACKED"; // PACKED, VERIFIED

    @Column(name = "packed_at")
    private LocalDateTime packedAt;
}
