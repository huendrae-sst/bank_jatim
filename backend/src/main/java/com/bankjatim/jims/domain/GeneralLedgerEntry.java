package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "general_ledger_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralLedgerEntry extends BaseEntity {

    @Column(name = "transaction_date", nullable = false)
    @Builder.Default
    private LocalDate transactionDate = LocalDate.now();

    @Column(name = "reference_number", nullable = false, length = 100)
    private String referenceNumber;

    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    @Column(name = "cost_center_code", length = 50)
    private String costCenterCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "debit_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;
}
