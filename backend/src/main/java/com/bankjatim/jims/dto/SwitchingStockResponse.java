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
            return new Summary(organization.getId(), organization.getCode(), organization.getName());
        }
    }

    public record WarehouseSummary(Long id, String code, String name) {
        static WarehouseSummary from(Warehouse warehouse) {
            if (warehouse == null) {
                return null;
            }
            return new WarehouseSummary(warehouse.getId(), warehouse.getCode(), warehouse.getName());
        }
    }

    public record UserSummary(Long id, String name, String nip, String email) {
        static UserSummary from(User user) {
            if (user == null) {
                return null;
            }
            return new UserSummary(user.getId(), user.getName(), user.getNip(), user.getEmail());
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
            return new ItemResponse(
                    switchingStockItem.getId(),
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
            return new ItemSummary(item.getId(), item.getSku(), item.getName(), item.getUom());
        }
    }
}
