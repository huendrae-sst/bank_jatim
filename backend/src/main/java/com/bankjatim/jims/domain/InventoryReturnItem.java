package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_return_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReturnItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_return_id", nullable = false)
    private InventoryReturn inventoryReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty_returned", nullable = false)
    @Builder.Default
    private Integer qtyReturned = 1;

    @Column(name = "qty_received", nullable = false)
    @Builder.Default
    private Integer qtyReceived = 0;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String condition = "GOOD"; // GOOD, DAMAGED, DEFECTIVE, OBSOLETE

    @Column(columnDefinition = "TEXT")
    private String notes;
}
