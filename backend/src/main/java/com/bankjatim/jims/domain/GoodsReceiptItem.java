package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "goods_receipt_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id", nullable = false)
    private PurchaseOrderItem purchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty_received", nullable = false)
    @Builder.Default
    private Integer qtyReceived = 0;

    @Column(name = "qty_accepted", nullable = false)
    @Builder.Default
    private Integer qtyAccepted = 0;

    @Column(name = "qty_rejected", nullable = false)
    @Builder.Default
    private Integer qtyRejected = 0;

    @Column(name = "condition_notes", columnDefinition = "TEXT")
    private String conditionNotes;
}
