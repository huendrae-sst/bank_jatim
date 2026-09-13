package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.StockLedger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockLedgerResponse(
        Long id,
        PurchaseOrderResponse.WarehouseSummary warehouse,
        OrderResponse.ItemSummary item,
        String transactionType,
        String referenceNumber,
        Integer qtyIn,
        Integer qtyOut,
        Integer balanceAfter,
        BigDecimal unitCost,
        BigDecimal totalValue,
        String notes,
        OrderResponse.UserSummary createdByUser,
        LocalDateTime createdAt
) {
    public static StockLedgerResponse from(StockLedger ledger) {
        return new StockLedgerResponse(
                ledger.getId(),
                PurchaseOrderResponse.WarehouseSummary.from(ledger.getWarehouse()),
                OrderResponse.ItemSummary.from(ledger.getItem()),
                ledger.getTransactionType(),
                ledger.getReferenceNumber(),
                ledger.getQtyIn(),
                ledger.getQtyOut(),
                ledger.getBalanceAfter(),
                ledger.getUnitCost(),
                ledger.getTotalValue(),
                ledger.getNotes(),
                OrderResponse.UserSummary.from(ledger.getCreatedByUser()),
                ledger.getCreatedAt()
        );
    }
}
