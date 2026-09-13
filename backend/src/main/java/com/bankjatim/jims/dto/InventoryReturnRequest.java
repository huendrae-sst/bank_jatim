package com.bankjatim.jims.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InventoryReturnRequest {
    private Long organizationId;
    @NotNull(message = "Gudang tujuan wajib dipilih")
    private Long destinationWarehouseId;
    @NotNull(message = "Alasan retur wajib diisi")
    private String reason;
    private String reasonDetails;
    private String orderRef;

    @NotEmpty(message = "Item retur tidak boleh kosong")
    private List<ItemLine> items;

    @Data
    public static class ItemLine {
        @NotNull(message = "Barang wajib dipilih")
        private Long itemId;
        @NotNull(message = "Jumlah retur wajib diisi")
        private Integer qty;
        private String condition;
        private String notes;
    }
}
