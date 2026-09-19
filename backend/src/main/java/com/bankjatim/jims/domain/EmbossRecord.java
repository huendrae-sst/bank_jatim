package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emboss_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbossRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emboss_file_id", nullable = false)
    private EmbossFile embossFile;

    @Column(name = "external_reference_id")
    private String externalReferenceId;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "card_number_masked", nullable = false, length = 50)
    private String cardNumberMasked;

    @Column(name = "card_type", nullable = false, length = 50)
    private String cardType; // GPN, MASTERCARD, VISA

    @Column(name = "branch_code", nullable = false, length = 50)
    private String branchCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_envelope_item_id")
    private Item pinEnvelopeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id")
    private ProductionOrder productionOrder;

    @Column(name = "production_status", length = 50)
    private String productionStatus; // NULL, QUEUED, PRODUCED, PRODUCTION_FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produced_item_id")
    private Item producedItem;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "VALID"; // VALID, REJECTED, PROCESSED

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}
