package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "cost_center_code", nullable = false, length = 50)
    private String costCenterCode;

    @Column(name = "year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "allocated_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(name = "committed_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal committedAmount = BigDecimal.ZERO;

    @Column(name = "realized_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal realizedAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Transient
    public BigDecimal getAvailableAmount() {
        return allocatedAmount.subtract(committedAmount).subtract(realizedAmount);
    }
}
