package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.InventoryReturn;
import com.bankjatim.jims.domain.InventoryReturnItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InventoryReturnResponse {
    private Long id;
    private String returnNumber;
    private String status;
    private String reason;
    private Long organizationId;
    private String organizationName;
    private Long destinationWarehouseId;
    private String destinationWarehouseName;
    private String requesterName;
    private String approverName;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
    private Integer totalQty;
    private BigDecimal totalValue;
    private List<ItemResponse> items;

    @Data
    @Builder
    public static class ItemResponse {
        private Long id;
        private Long itemId;
        private String sku;
        private String name;
        private String category;
        private String uom;
        private String condition;
        private Integer qtyReturned;
        private Integer qtyReceived;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String notes;
    }

    public static InventoryReturnResponse from(InventoryReturn r) {
        int totalQ = 0;
        BigDecimal totalV = BigDecimal.ZERO;

        List<ItemResponse> itemResponses = null;
        if (r.getItems() != null) {
            itemResponses = r.getItems().stream().map(i -> {
                BigDecimal price = i.getItem() != null && i.getItem().getEstimatedUnitPrice() != null
                        ? i.getItem().getEstimatedUnitPrice() : BigDecimal.ZERO;
                BigDecimal sub = price.multiply(BigDecimal.valueOf(i.getQtyReturned() != null ? i.getQtyReturned() : 0));
                return ItemResponse.builder()
                        .id(i.getId())
                        .itemId(i.getItem() != null ? i.getItem().getId() : null)
                        .sku(i.getItem() != null ? i.getItem().getSku() : "-")
                        .name(i.getItem() != null ? i.getItem().getName() : "-")
                        .category(i.getItem() != null && i.getItem().getCategory() != null ? i.getItem().getCategory().getName() : "-")
                        .uom(i.getItem() != null ? i.getItem().getUom() : "PCS")
                        .condition(i.getCondition())
                        .qtyReturned(i.getQtyReturned())
                        .qtyReceived(i.getQtyReceived())
                        .unitPrice(price)
                        .subtotal(sub)
                        .notes(i.getNotes())
                        .build();
            }).toList();

            for (ItemResponse ir : itemResponses) {
                totalQ += ir.getQtyReturned() != null ? ir.getQtyReturned() : 0;
                totalV = totalV.add(ir.getSubtotal());
            }
        }

        return InventoryReturnResponse.builder()
                .id(r.getId())
                .returnNumber(r.getReturnNumber())
                .status(r.getStatus())
                .reason(r.getReason())
                .organizationId(r.getOrganization() != null ? r.getOrganization().getId() : null)
                .organizationName(r.getOrganization() != null ? r.getOrganization().getName() : "-")
                .destinationWarehouseId(r.getDestinationWarehouse() != null ? r.getDestinationWarehouse().getId() : null)
                .destinationWarehouseName(r.getDestinationWarehouse() != null ? r.getDestinationWarehouse().getName() : "-")
                .requesterName(r.getCreatedByUser() != null ? r.getCreatedByUser().getName() : "-")
                .approverName(r.getApprovedByUser() != null ? r.getApprovedByUser().getName() : null)
                .shippedAt(r.getShippedAt())
                .receivedAt(r.getReceivedAt())
                .createdAt(r.getCreatedAt())
                .totalQty(totalQ)
                .totalValue(totalV)
                .items(itemResponses)
                .build();
    }
}
