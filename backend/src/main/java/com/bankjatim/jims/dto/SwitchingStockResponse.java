package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.SwitchingStock;
import com.bankjatim.jims.domain.SwitchingStockItem;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.domain.Warehouse;

import java.time.LocalDateTime;
import java.util.List;

public record SwitchingStockResponse(
        Long id,
        String switchNo,
        String status,
        Summary sourceOrganization,
        WarehouseSummary sourceWarehouse,
        Summary destinationOrganization,
        WarehouseSummary destinationWarehouse,
        UserSummary proposedByUser,
        UserSummary approvedByUser,
        String recommendationReason,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ItemResponse> items
) {
    public static SwitchingStockResponse from(SwitchingStock switchingStock) {
        return new SwitchingStockResponse(
                switchingStock.getId(),
                "SW-" + switchingStock.getId(),
                switchingStock.getStatus(),
                Summary.from(switchingStock.getSourceOrganization()),
                WarehouseSummary.from(switchingStock.getSourceWarehouse()),
                Summary.from(switchingStock.getDestinationOrganization()),
                WarehouseSummary.from(switchingStock.getDestinationWarehouse()),
                UserSummary.from(switchingStock.getProposedByUser()),
                UserSummary.from(switchingStock.getApprovedByUser()),
                switchingStock.getRecommendationReason(),
                switchingStock.getRejectionReason(),
                switchingStock.getCreatedAt(),
                switchingStock.getUpdatedAt(),
                switchingStock.getItems().stream().map(ItemResponse::from).toList()
        );
    }

    public record Summary(Long id, String code, String name) {
        static Summary from(Organization organization) {
            if (organization == null) {
                return null;
            }
            Long id = com.bankjatim.jims.common.HibernateUtils.getId(organization);
            try {
                if (com.bankjatim.jims.common.HibernateUtils.isInitialized(organization)) {
                    return new Summary(id, organization.getCode(), organization.getName());
                }
            } catch (Exception ignored) {
            }
            return new Summary(id, null, null);
        }
    }

    public record WarehouseSummary(Long id, String code, String name) {
        static WarehouseSummary from(Warehouse warehouse) {
            if (warehouse == null) {
                return null;
            }
            Long id = com.bankjatim.jims.common.HibernateUtils.getId(warehouse);
            try {
                if (com.bankjatim.jims.common.HibernateUtils.isInitialized(warehouse)) {
                    return new WarehouseSummary(id, warehouse.getCode(), warehouse.getName());
                }
            } catch (Exception ignored) {
            }
            return new WarehouseSummary(id, null, null);
        }
    }

    public record UserSummary(Long id, String name, String nip, String email) {
        static UserSummary from(User user) {
            if (user == null) {
                return null;
            }
            Long id = com.bankjatim.jims.common.HibernateUtils.getId(user);
            try {
                if (com.bankjatim.jims.common.HibernateUtils.isInitialized(user)) {
                    return new UserSummary(id, user.getName(), user.getNip(), user.getEmail());
                }
            } catch (Exception ignored) {
            }
            return new UserSummary(id, null, null, null);
        }
    }

    public record ItemResponse(
            Long id,
            ItemSummary item,
            Integer qtyRequested,
            Integer qtyApproved,
            Integer qtyTransferred,
            Integer qtyReceived,
            String notes
    ) {
        static ItemResponse from(SwitchingStockItem switchingStockItem) {
            if (switchingStockItem == null) return null;
            Long id = com.bankjatim.jims.common.HibernateUtils.getId(switchingStockItem);
            return new ItemResponse(
                    id,
                    ItemSummary.from(switchingStockItem.getItem()),
                    switchingStockItem.getQtyRequested(),
                    switchingStockItem.getQtyApproved(),
                    switchingStockItem.getQtyTransferred(),
                    switchingStockItem.getQtyReceived(),
                    switchingStockItem.getNotes()
            );
        }
    }

    public record ItemSummary(Long id, String sku, String name, String uom) {
        static ItemSummary from(Item item) {
            if (item == null) {
                return null;
            }
            Long id = com.bankjatim.jims.common.HibernateUtils.getId(item);
            try {
                if (com.bankjatim.jims.common.HibernateUtils.isInitialized(item)) {
                    return new ItemSummary(id, item.getSku(), item.getName(), item.getUom());
                }
            } catch (Exception ignored) {
            }
            return new ItemSummary(id, null, null, null);
        }
    }
}
