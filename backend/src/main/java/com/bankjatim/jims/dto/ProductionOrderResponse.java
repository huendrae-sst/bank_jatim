package com.bankjatim.jims.dto;

import com.bankjatim.jims.common.HibernateUtils;
import com.bankjatim.jims.domain.ProductionOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record ProductionOrderResponse(
        Long id,
        String productionNumber,
        WarehouseSummary warehouse,
        OrderResponse.UserSummary createdByUser,
        LocalDate productionDate,
        Integer totalQty,
        Integer totalProduced,
        Integer totalDamaged,
        String status,
        String materialIssueStatus,
        OrderResponse.UserSummary approvedByUser,
        LocalDateTime approvedAt,
        OrderResponse.UserSummary materialIssuedByUser,
        LocalDateTime materialIssuedAt,
        Long embossFileId,
        String embossFileName,
        LocalDateTime completedAt,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProductionOrderItemResponse> items,
        List<FulfillmentSummary> fulfillments
) {
    public record WarehouseSummary(Long id, String code, String name) {}
    public record FulfillmentSummary(Long id, Long orderId, String orderNumber, String branchName, Integer qtyFulfilled, LocalDateTime fulfilledAt) {}

    public static ProductionOrderResponse from(ProductionOrder po) {
        if (po == null) return null;

        WarehouseSummary wh = null;
        if (po.getWarehouse() != null) {
            wh = new WarehouseSummary(
                    HibernateUtils.getId(po.getWarehouse()),
                    po.getWarehouse().getCode(),
                    po.getWarehouse().getName()
            );
        }

        OrderResponse.UserSummary creator = OrderResponse.UserSummary.from(po.getCreatedByUser());
        OrderResponse.UserSummary approver = OrderResponse.UserSummary.from(po.getApprovedByUser());
        OrderResponse.UserSummary issuer = OrderResponse.UserSummary.from(po.getMaterialIssuedByUser());

        Long embId = po.getEmbossFile() != null ? HibernateUtils.getId(po.getEmbossFile()) : null;
        String embName = po.getEmbossFile() != null ? po.getEmbossFile().getFilename() : null;

        List<ProductionOrderItemResponse> itemsList = Collections.emptyList();
        try {
            if (po.getItems() != null && HibernateUtils.isInitialized(po.getItems())) {
                itemsList = po.getItems().stream().map(ProductionOrderItemResponse::from).toList();
            }
        } catch (Exception ignored) {}

        List<FulfillmentSummary> fulList = Collections.emptyList();
        try {
            if (po.getFulfillments() != null && HibernateUtils.isInitialized(po.getFulfillments())) {
                fulList = po.getFulfillments().stream().map(f -> new FulfillmentSummary(
                        f.getId(),
                        f.getOrder() != null ? HibernateUtils.getId(f.getOrder()) : null,
                        f.getOrder() != null ? f.getOrder().getOrderNumber() : null,
                        f.getOrder() != null && f.getOrder().getRequestingOrganization() != null ? f.getOrder().getRequestingOrganization().getName() : null,
                        f.getQtyFulfilled(),
                        f.getFulfilledAt()
                )).toList();
            }
        } catch (Exception ignored) {}

        return new ProductionOrderResponse(
                po.getId(),
                po.getProductionNumber(),
                wh,
                creator,
                po.getProductionDate(),
                po.getTotalQty(),
                po.getTotalProduced(),
                po.getTotalDamaged(),
                po.getStatus(),
                po.getMaterialIssueStatus(),
                approver,
                po.getApprovedAt(),
                issuer,
                po.getMaterialIssuedAt(),
                embId,
                embName,
                po.getCompletedAt(),
                po.getNotes(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                itemsList,
                fulList
        );
    }
}
