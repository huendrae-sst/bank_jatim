package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.GoodsReceipt;
import com.bankjatim.jims.domain.GoodsReceiptItem;

import java.time.LocalDate;
import java.util.List;

public record GoodsReceiptResponse(
        Long id,
        String grnNumber,
        PurchaseOrderResponse po,
        PurchaseOrderResponse.WarehouseSummary warehouse,
        OrderResponse.UserSummary receivedByUser,
        String vendorDeliveryNoteNumber,
        LocalDate receiptDate,
        String status,
        String notes,
        List<ItemResponse> items
) {
    public static GoodsReceiptResponse from(GoodsReceipt receipt) {
        return new GoodsReceiptResponse(
                receipt.getId(),
                receipt.getGrnNumber(),
                PurchaseOrderResponse.from(receipt.getPurchaseOrder()),
                PurchaseOrderResponse.WarehouseSummary.from(receipt.getWarehouse()),
                OrderResponse.UserSummary.from(receipt.getReceivedByUser()),
                receipt.getVendorDeliveryNoteNumber(),
                receipt.getReceiptDate(),
                receipt.getStatus(),
                receipt.getNotes(),
                receipt.getItems().stream().map(ItemResponse::from).toList()
        );
    }

    public record ItemResponse(
            Long id,
            OrderResponse.ItemSummary item,
            Integer qtyReceived,
            Integer qtyAccepted,
            Integer qtyRejected,
            String conditionNotes
    ) {
        static ItemResponse from(GoodsReceiptItem item) {
            return new ItemResponse(
                    item.getId(),
                    OrderResponse.ItemSummary.from(item.getItem()),
                    item.getQtyReceived(),
                    item.getQtyAccepted(),
                    item.getQtyRejected(),
                    item.getConditionNotes()
            );
        }
    }
}
