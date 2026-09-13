package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "discrepancies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discrepancy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_id", nullable = false)
    private Receiving receiving;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "discrepancy_type", nullable = false, length = 50)
    @Builder.Default
    private String discrepancyType = "DAMAGED"; // MISSING, DAMAGED, WRONG_ITEM, EXCESS

    @Column(name = "qty_expected", nullable = false)
    @Builder.Default
    private Integer qtyExpected = 0;

    @Column(name = "qty_actual", nullable = false)
    @Builder.Default
    private Integer qtyActual = 0;

    @Column(name = "qty_damaged", nullable = false)
    @Builder.Default
    private Integer qtyDamaged = 0;

    @Column(name = "resolution_status", nullable = false, length = 50)
    @Builder.Default
    private String resolutionStatus = "REPORTED"; // REPORTED, UNDER_REVIEW, RESOLVED, CLAIMED

    @Column(name = "berita_acara_number", length = 100)
    private String beritaAcaraNumber;

    @Column(name = "berita_acara_url", columnDefinition = "TEXT")
    private String beritaAcaraUrl;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
}
