package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_returns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReturn extends BaseEntity {

    @Column(name = "return_number", nullable = false, unique = true, length = 100)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "REQUESTED"; // REQUESTED, APPROVED, SHIPPED, RECEIVED, REJECTED

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @OneToMany(mappedBy = "inventoryReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InventoryReturnItem> items = new ArrayList<>();

    public void addItem(InventoryReturnItem item) {
        items.add(item);
        item.setInventoryReturn(this);
    }
}
