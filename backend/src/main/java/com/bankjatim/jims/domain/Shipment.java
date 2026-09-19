package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment extends BaseEntity {

    @Column(name = "manifest_number", nullable = false, unique = true, length = 100)
    private String manifestNumber;

    @Column(name = "distribution_type", nullable = false, length = 50)
    @Builder.Default
    private String distributionType = "ORDER_REQUEST"; // ORDER_REQUEST, PURCHASE_REQUEST, EMBOSS, ROUTINE_PUSH

    @Column(name = "batch_manifest_number", length = 100)
    private String batchManifestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "switching_stock_id")
    private SwitchingStock switchingStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_warehouse_id", nullable = false)
    private Warehouse originWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_organization_id", nullable = false)
    private Organization destinationOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @Column(name = "service_type", nullable = false, length = 50)
    @Builder.Default
    private String serviceType = "REGULER";

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatched_by_user_id", nullable = false)
    private User dispatchedByUser;

    @Column(name = "koli_count", nullable = false)
    @Builder.Default
    private Integer koliCount = 1;

    @Column(name = "total_weight_kg", precision = 8, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalWeightKg = new BigDecimal("1.00");

    @Column(name = "shipping_cost", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "eta_date")
    private LocalDate etaDate;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "CREATED";

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<ShipmentItem> items = new java.util.ArrayList<>();
}
