package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest extends BaseEntity {

    @Column(name = "pr_number", nullable = false, unique = true, length = 100)
    private String prNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(name = "procurement_method", nullable = false, length = 50)
    @Builder.Default
    private String procurementMethod = "DIRECT";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String purpose;

    @Column(name = "estimated_total_cost", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal estimatedTotalCost = BigDecimal.ZERO;

    @Column(name = "budget_status", nullable = false, length = 50)
    @Builder.Default
    private String budgetStatus = "VALIDATED";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseRequestItem> items = new ArrayList<>();
}
