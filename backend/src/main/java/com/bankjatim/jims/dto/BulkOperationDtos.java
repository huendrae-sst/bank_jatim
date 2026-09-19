package com.bankjatim.jims.dto;

import java.math.BigDecimal;
import java.util.List;

public class BulkOperationDtos {

    public record BatchPickingRequest(
            List<Long> orderIds
    ) {}

    public record BatchPackingRequest(
            List<PackingItem> items
    ) {
        public record PackingItem(
                Long orderId,
                int koliCount,
                BigDecimal totalWeightKg,
                String dimensionsCm
        ) {}
    }

    public record BulkShipmentRequest(
            List<Long> orderIds,
            Long courierId,
            String serviceType,
            BigDecimal shippingCostPerOrder,
            String batchManifestNumber,
            String trackingNumberPrefix
    ) {}

    public record RoutineDistributionRequest(
            List<Long> organizationIds,
            String routinePeriod,
            String notes,
            List<RoutineItem> items,
            List<BranchAllocation> branchAllocations
    ) {
        public record RoutineItem(
                Long itemId,
                int qty
        ) {}

        public record BranchAllocation(
                Long organizationId,
                List<RoutineItem> items
        ) {}
    }

    public record EmbossOrderCreateRequest(
            Long organizationId,
            String notes,
            List<EmbossRecordRow> records
    ) {
        public record EmbossRecordRow(
                String accountNumber,
                String customerName,
                String cardNumberMasked,
                String cardType, // GPN, MASTERCARD
                String branchCode
        ) {}
    }
}
