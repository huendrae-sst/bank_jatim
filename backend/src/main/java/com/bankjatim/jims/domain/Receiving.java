package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receivings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receiving extends BaseEntity {

    @Column(name = "receiving_number", nullable = false, unique = true, length = 100)
    private String receivingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "switching_stock_id")
    private SwitchingStock switchingStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_user_id", nullable = false)
    private User receivedByUser;

    @Column(name = "receipt_date", nullable = false)
    @Builder.Default
    private LocalDate receiptDate = LocalDate.now();

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "RECEIVED_FULL"; // RECEIVED_FULL, RECEIVED_PARTIAL, DISCREPANCY

    @Column(name = "pod_signature", columnDefinition = "TEXT")
    private String podSignature;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "receiving", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Discrepancy> discrepancies = new ArrayList<>();
}
