package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement extends BaseEntity {

    @Column(name = "settlement_number", nullable = false, unique = true, length = 100)
    private String settlementNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_organization_id", nullable = false)
    private Organization debitOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_organization_id", nullable = false)
    private Organization creditOrganization;

    @Column(name = "debit_cost_center", nullable = false, length = 50)
    private String debitCostCenter;

    @Column(name = "credit_cost_center", nullable = false, length = 50)
    private String creditCostCenter;

    @Column(name = "item_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal itemAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;
}
