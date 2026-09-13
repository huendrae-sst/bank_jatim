package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Category;
import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.StockBalance;
import com.bankjatim.jims.domain.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockBalanceResponse(
        Long id,
        WarehouseSummary warehouse,
        ItemSummary item,
        Integer onHand,
        Integer reserved,
        Integer allocated,
        Integer inTransit,
        Integer hold,
        Integer damaged,
        Integer minStockOverride,
        Integer maxStockOverride,
        Integer availableStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StockBalanceResponse from(StockBalance stockBalance) {
        return new StockBalanceResponse(
                stockBalance.getId(),
                WarehouseSummary.from(stockBalance.getWarehouse()),
                ItemSummary.from(stockBalance.getItem()),
                stockBalance.getOnHand(),
                stockBalance.getReserved(),
                stockBalance.getAllocated(),
                stockBalance.getInTransit(),
                stockBalance.getHold(),
                stockBalance.getDamaged(),
                stockBalance.getMinStockOverride(),
                stockBalance.getMaxStockOverride(),
                stockBalance.getAvailableStock(),
                stockBalance.getCreatedAt(),
                stockBalance.getUpdatedAt()
        );
    }

    public record WarehouseSummary(Long id, String code, String name, String type) {
        static WarehouseSummary from(Warehouse warehouse) {
            if (warehouse == null) {
                return null;
            }
            return new WarehouseSummary(
                    warehouse.getId(),
                    warehouse.getCode(),
                    warehouse.getName(),
                    warehouse.getType()
            );
        }
    }

    public record ItemSummary(
            Long id,
            String sku,
            String barcode,
            String name,
            String uom,
            BigDecimal estimatedUnitPrice,
            CategorySummary category
    ) {
        static ItemSummary from(Item item) {
            if (item == null) {
                return null;
            }
            return new ItemSummary(
                    item.getId(),
                    item.getSku(),
                    item.getBarcode(),
                    item.getName(),
                    item.getUom(),
                    item.getEstimatedUnitPrice(),
                    CategorySummary.from(item.getCategory())
            );
        }
    }

    public record CategorySummary(Long id, String code, String name) {
        static CategorySummary from(Category category) {
            if (category == null) {
                return null;
            }
            return new CategorySummary(category.getId(), category.getCode(), category.getName());
        }
    }
}
