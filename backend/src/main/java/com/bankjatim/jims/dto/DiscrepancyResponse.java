package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Discrepancy;

public record DiscrepancyResponse(
        Long id,
        Long receivingId,
        String receivingNumber,
        String orderNumber,
        OrderResponse.ItemSummary item,
        String discrepancyType,
        Integer qtyExpected,
        Integer qtyActual,
        Integer qtyDamaged,
        String resolutionStatus,
        String beritaAcaraNumber,
        String beritaAcaraUrl,
        String resolutionNotes
) {
    public static DiscrepancyResponse from(Discrepancy discrepancy) {
        return new DiscrepancyResponse(
                discrepancy.getId(),
                discrepancy.getReceiving() != null ? discrepancy.getReceiving().getId() : null,
                discrepancy.getReceiving() != null ? discrepancy.getReceiving().getReceivingNumber() : null,
                discrepancy.getReceiving() != null && discrepancy.getReceiving().getOrder() != null
                        ? discrepancy.getReceiving().getOrder().getOrderNumber()
                        : null,
                OrderResponse.ItemSummary.from(discrepancy.getItem()),
                discrepancy.getDiscrepancyType(),
                discrepancy.getQtyExpected(),
                discrepancy.getQtyActual(),
                discrepancy.getQtyDamaged(),
                discrepancy.getResolutionStatus(),
                discrepancy.getBeritaAcaraNumber(),
                discrepancy.getBeritaAcaraUrl(),
                discrepancy.getResolutionNotes()
        );
    }
}
