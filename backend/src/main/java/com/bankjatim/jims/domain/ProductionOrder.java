package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrder extends BaseEntity {

    @Column(name = "production_number", nullable = false, unique = true, length = 100)
    private String productionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(name = "production_date", nullable = false)
    @Builder.Default
    private LocalDate productionDate = LocalDate.now();

    @Column(name = "total_qty", nullable = false)
    @Builder.Default
    private Integer totalQty = 0;

    @Column(name = "total_produced", nullable = false)
    @Builder.Default
    private Integer totalProduced = 0;

    @Column(name = "total_damaged", nullable = false)
    @Builder.Default
    private Integer totalDamaged = 0;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, MATERIAL_REQUESTED, MATERIAL_APPROVED, IN_PRODUCTION, QC_REVIEW, COMPLETED, CANCELLED

    @Column(name = "material_issue_status", nullable = false, length = 50)
    @Builder.Default
    private String materialIssueStatus = "PENDING"; // PENDING, REQUESTED, APPROVED, ISSUED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_issued_by_user_id")
    private User materialIssuedByUser;

    @Column(name = "material_issued_at")
    private LocalDateTime materialIssuedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emboss_file_id")
    private EmbossFile embossFile;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductionOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductionOrderFulfillment> fulfillments = new ArrayList<>();
}
