package com.bankjatim.jims.dto;

import com.bankjatim.jims.common.HibernateUtils;
import com.bankjatim.jims.domain.ItemBom;

public record ItemBomResponse(
        Long id,
        Long finishedItemId,
        String finishedSku,
        String finishedName,
        Long materialItemId,
        String materialSku,
        String materialName,
        Integer qtyPerUnit,
        String notes
) {
    public static ItemBomResponse from(ItemBom b) {
        if (b == null) return null;
        return new ItemBomResponse(
                b.getId(),
                b.getFinishedItem() != null ? HibernateUtils.getId(b.getFinishedItem()) : null,
                b.getFinishedItem() != null ? b.getFinishedItem().getSku() : null,
                b.getFinishedItem() != null ? b.getFinishedItem().getName() : null,
                b.getMaterialItem() != null ? HibernateUtils.getId(b.getMaterialItem()) : null,
                b.getMaterialItem() != null ? b.getMaterialItem().getSku() : null,
                b.getMaterialItem() != null ? b.getMaterialItem().getName() : null,
                b.getQtyPerUnit(),
                b.getNotes()
        );
    }
}
