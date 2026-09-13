package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(unique = true, length = 100)
    private String barcode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String uom = "PCS";

    @Column(columnDefinition = "TEXT")
    private String specification;

    @Column(name = "min_stock", nullable = false)
    @Builder.Default
    private Integer minStock = 10;

    @Column(name = "max_stock", nullable = false)
    @Builder.Default
    private Integer maxStock = 500;

    @Column(name = "safety_stock", nullable = false)
    @Builder.Default
    private Integer safetyStock = 20;

    @Column(name = "reorder_point", nullable = false)
    @Builder.Default
    private Integer reorderPoint = 30;

    @Column(name = "lead_time_days", nullable = false)
    @Builder.Default
    private Integer leadTimeDays = 5;

    @Column(name = "estimated_unit_price", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal estimatedUnitPrice = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
