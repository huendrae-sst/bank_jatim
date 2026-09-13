package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receipts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceipt extends BaseEntity {

    @Column(name = "grn_number", nullable = false, unique = true, length = 100)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_user_id", nullable = false)
    private User receivedByUser;

    @Column(name = "vendor_delivery_note_number", length = 100)
    private String vendorDeliveryNoteNumber;

    @Column(name = "receipt_date", nullable = false)
    @Builder.Default
    private LocalDate receiptDate = LocalDate.now();

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "RECEIVED";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsReceiptItem> items = new ArrayList<>();
}
