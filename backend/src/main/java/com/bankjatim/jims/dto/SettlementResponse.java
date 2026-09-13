package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        String settlementNumber,
        Long orderId,
        String orderNumber,
        OrderResponse.OrganizationSummary debitOrganization,
        OrderResponse.OrganizationSummary creditOrganization,
        String debitCostCenter,
        String creditCostCenter,
        BigDecimal itemAmount,
        BigDecimal shippingAmount,
        BigDecimal totalAmount,
        String status,
        OrderResponse.UserSummary createdByUser,
        OrderResponse.UserSummary approvedByUser,
        LocalDateTime postedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getSettlementNumber(),
                settlement.getOrder() != null ? settlement.getOrder().getId() : null,
                settlement.getOrder() != null ? settlement.getOrder().getOrderNumber() : null,
                OrderResponse.OrganizationSummary.from(settlement.getDebitOrganization()),
                OrderResponse.OrganizationSummary.from(settlement.getCreditOrganization()),
                settlement.getDebitCostCenter(),
                settlement.getCreditCostCenter(),
                settlement.getItemAmount(),
                settlement.getShippingAmount(),
                settlement.getTotalAmount(),
                settlement.getStatus(),
                OrderResponse.UserSummary.from(settlement.getCreatedByUser()),
                OrderResponse.UserSummary.from(settlement.getApprovedByUser()),
                settlement.getPostedAt(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}
