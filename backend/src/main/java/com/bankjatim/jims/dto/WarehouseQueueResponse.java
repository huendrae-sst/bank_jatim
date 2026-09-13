package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Order;

import java.math.BigDecimal;
import java.util.List;

public record WarehouseQueueResponse(
        Long orderId,
        String orderNumber,
        String branch,
        String branchCode,
        String warehouse,
        String priority,
        String status,
        BigDecimal totalEstimatedValue,
        List<ItemLine> items
) {
    public static WarehouseQueueResponse from(Order order) {
        return new WarehouseQueueResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getRequestingOrganization() != null ? order.getRequestingOrganization().getName() : "-",
                order.getRequestingOrganization() != null ? order.getRequestingOrganization().getCode() : "-",
                order.getRequestingWarehouse() != null ? order.getRequestingWarehouse().getName() : "-",
                order.getPriority(),
                order.getStatus(),
                order.getTotalEstimatedValue(),
                order.getItems().stream()
                        .map(item -> new ItemLine(
                                item.getId(),
                                OrderResponse.ItemSummary.from(item.getItem()),
                                item.getQtyApproved(),
                                item.getQtyPicked(),
                                item.getQtyPacked()
                        ))
                        .toList()
        );
    }

    public record ItemLine(
            Long id,
            OrderResponse.ItemSummary item,
            Integer qtyApproved,
            Integer qtyPicked,
            Integer qtyPacked
    ) {
    }
}
