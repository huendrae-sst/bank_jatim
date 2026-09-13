package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.PurchaseRequest;
import com.bankjatim.jims.domain.PurchaseRequestItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseRequestResponse(
        Long id,
        String prNumber,
        OrderResponse.OrganizationSummary organization,
        OrderResponse.UserSummary createdByUser,
        OrderResponse.UserSummary approvedByUser,
        String procurementMethod,
        String purpose,
        BigDecimal estimatedTotalCost,
        String budgetStatus,
        String status,
        String rejectionReason,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ItemResponse> items
) {
    public static PurchaseRequestResponse from(PurchaseRequest pr) {
        return new PurchaseRequestResponse(
                pr.getId(),
                pr.getPrNumber(),
                OrderResponse.OrganizationSummary.from(pr.getOrganization()),
                OrderResponse.UserSummary.from(pr.getCreatedByUser()),
                OrderResponse.UserSummary.from(pr.getApprovedByUser()),
                pr.getProcurementMethod(),
                pr.getPurpose(),
                pr.getEstimatedTotalCost(),
                pr.getBudgetStatus(),
                pr.getStatus(),
                pr.getRejectionReason(),
                pr.getSubmittedAt(),
                pr.getApprovedAt(),
                pr.getCreatedAt(),
                pr.getUpdatedAt(),
                pr.getItems().stream().map(ItemResponse::from).toList()
        );
    }

    public record ItemResponse(
            Long id,
            OrderResponse.ItemSummary item,
            Integer qtyRequested,
            Integer qtyApproved,
            Integer qtyOrdered,
            BigDecimal estimatedUnitPrice,
            BigDecimal estimatedSubtotal
    ) {
        static ItemResponse from(PurchaseRequestItem item) {
            return new ItemResponse(
                    item.getId(),
                    OrderResponse.ItemSummary.from(item.getItem()),
                    item.getQtyRequested(),
                    item.getQtyApproved(),
                    item.getQtyOrdered(),
                    item.getEstimatedUnitPrice(),
                    item.getEstimatedSubtotal()
            );
        }
    }
}
