package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.EmbossRecord;
import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.Order;

public record EmbossRecordResponse(
        Long id,
        Long embossFileId,
        String accountNumber,
        String customerName,
        String cardNumberMasked,
        String cardType,
        String branchCode,
        ItemSummary item,
        ItemSummary pinEnvelopeItem,
        OrderSummary order,
        Long productionOrderId,
        String productionNumber,
        String productionStatus,
        ItemSummary producedItem,
        String status,
        String rejectionReason
) {
    public static EmbossRecordResponse from(EmbossRecord record) {
        return new EmbossRecordResponse(
                record.getId(),
                record.getEmbossFile() != null ? record.getEmbossFile().getId() : null,
                record.getAccountNumber(),
                record.getCustomerName(),
                record.getCardNumberMasked(),
                record.getCardType(),
                record.getBranchCode(),
                ItemSummary.from(record.getItem()),
                ItemSummary.from(record.getPinEnvelopeItem()),
                OrderSummary.from(record.getOrder()),
                record.getProductionOrder() != null ? record.getProductionOrder().getId() : null,
                record.getProductionOrder() != null ? record.getProductionOrder().getProductionNumber() : null,
                record.getProductionStatus(),
                ItemSummary.from(record.getProducedItem()),
                record.getStatus(),
                record.getRejectionReason()
        );
    }

    public record ItemSummary(Long id, String sku, String name, String uom) {
        static ItemSummary from(Item item) {
            if (item == null) {
                return null;
            }
            return new ItemSummary(item.getId(), item.getSku(), item.getName(), item.getUom());
        }
    }

    public record OrderSummary(Long id, String orderNumber) {
        static OrderSummary from(Order order) {
            if (order == null) {
                return null;
            }
            return new OrderSummary(order.getId(), order.getOrderNumber());
        }
    }
}
