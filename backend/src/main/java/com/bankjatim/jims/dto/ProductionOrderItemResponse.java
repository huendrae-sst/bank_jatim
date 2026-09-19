package com.bankjatim.jims.dto;

import com.bankjatim.jims.common.HibernateUtils;
import com.bankjatim.jims.domain.ProductionOrderItem;

public record ProductionOrderItemResponse(
        Long id,
        Long itemId,
        String itemSku,
        String itemName,
        String uom,
        Integer qtyPlanned,
        Integer qtyProduced,
        Integer qtyDamaged,
        String materialSku,
        String materialName
) {
    public static ProductionOrderItemResponse from(ProductionOrderItem item) {
        if (item == null) return null;
        Long id = HibernateUtils.getId(item);
        Long itemId = item.getItem() != null ? HibernateUtils.getId(item.getItem()) : null;
        String sku = item.getItem() != null ? item.getItem().getSku() : "-";
        String name = item.getItem() != null ? item.getItem().getName() : "-";
        String uom = item.getItem() != null ? item.getItem().getUom() : "PCS";

        return new ProductionOrderItemResponse(
                id,
                itemId,
                sku,
                name,
                uom,
                item.getQtyPlanned(),
                item.getQtyProduced(),
                item.getQtyDamaged(),
                null,
                null
        );
    }

    public static ProductionOrderItemResponse fromWithMaterial(ProductionOrderItem item, String matSku, String matName) {
        if (item == null) return null;
        Long id = HibernateUtils.getId(item);
        Long itemId = item.getItem() != null ? HibernateUtils.getId(item.getItem()) : null;
        String sku = item.getItem() != null ? item.getItem().getSku() : "-";
        String name = item.getItem() != null ? item.getItem().getName() : "-";
        String uom = item.getItem() != null ? item.getItem().getUom() : "PCS";

        return new ProductionOrderItemResponse(
                id,
                itemId,
                sku,
                name,
                uom,
                item.getQtyPlanned(),
                item.getQtyProduced(),
                item.getQtyDamaged(),
                matSku,
                matName
        );
    }
}
