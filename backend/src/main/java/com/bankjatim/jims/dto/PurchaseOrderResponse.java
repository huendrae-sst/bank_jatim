package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.PurchaseOrder;
import com.bankjatim.jims.domain.PurchaseOrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String poNumber,
        VendorSummary vendor,
        WarehouseSummary warehouse,
        OrderResponse.UserSummary createdByUser,
        OrderResponse.UserSummary approvedByUser,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String status,
        String rejectionReason,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ItemResponse> items
) {
    public static PurchaseOrderResponse from(PurchaseOrder po) {
        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                VendorSummary.from(po.getVendor()),
                WarehouseSummary.from(po.getWarehouse()),
                OrderResponse.UserSummary.from(po.getCreatedByUser()),
                OrderResponse.UserSummary.from(po.getApprovedByUser()),
                po.getOrderDate(),
                po.getExpectedDeliveryDate(),
                po.getSubtotal(),
                po.getTaxAmount(),
                po.getTotalAmount(),
                po.getStatus(),
                po.getRejectionReason(),
                po.getNotes(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                po.getItems().stream().map(ItemResponse::from).toList()
        );
    }

    public record VendorSummary(Long id, String code, String name) {
        static VendorSummary from(com.bankjatim.jims.domain.Vendor vendor) {
            if (vendor == null) {
                return null;
            }
            return new VendorSummary(vendor.getId(), vendor.getCode(), vendor.getName());
        }
    }

    public record WarehouseSummary(Long id, String code, String name) {
        static WarehouseSummary from(com.bankjatim.jims.domain.Warehouse warehouse) {
            if (warehouse == null) {
                return null;
            }
            return new WarehouseSummary(warehouse.getId(), warehouse.getCode(), warehouse.getName());
        }
    }

    public record ItemResponse(
            Long id,
            OrderResponse.ItemSummary item,
            Integer qtyOrdered,
            Integer qtyReceived,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        static ItemResponse from(PurchaseOrderItem item) {
            return new ItemResponse(
                    item.getId(),
                    OrderResponse.ItemSummary.from(item.getItem()),
                    item.getQtyOrdered(),
                    item.getQtyReceived(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            );
        }
    }
}
