package com.bankjatim.jims.dto;

import java.util.List;

public record RecordProductionResultRequest(
        List<ItemResult> items,
        List<CardResult> cards
) {
    public record ItemResult(
            Long productionOrderItemId,
            int qtyProduced,
            int qtyDamaged
    ) {}

    public record CardResult(
            Long embossRecordId,
            String result // "PRODUCED" or "PRODUCTION_FAILED"
    ) {}
}
