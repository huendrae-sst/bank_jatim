package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_bom", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"finished_item_id", "material_item_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemBom extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finished_item_id", nullable = false)
    private Item finishedItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_item_id", nullable = false)
    private Item materialItem;

    @Column(name = "qty_per_unit", nullable = false)
    @Builder.Default
    private Integer qtyPerUnit = 1;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
