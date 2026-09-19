package com.bankjatim.jims.dto;

import com.bankjatim.jims.common.HibernateUtils;
import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.Order;
import com.bankjatim.jims.domain.OrderItem;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrganizationSummary requestingOrganization,
        UserSummary createdByUser,
        UserSummary approvedByUser,
        LocalDate requiredDate,
        String priority,
        String status,
        String orderType,
        String fulfillmentStatus,
        Long purchaseRequestId,
        Long embossFileId,
        String batchManifestNumber,
        String routinePeriod,
        Boolean isOverbudget,
        Boolean isPickupKp,
        String deliveryMethod,
        BigDecimal totalEstimatedValue,
        Integer totalItems,
        String rejectionReason,
        String notes,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        if (order == null) {
            return null;
        }

        Long id = HibernateUtils.getId(order);

        List<OrderItemResponse> itemsList = Collections.emptyList();
        try {
            if (order.getItems() != null && HibernateUtils.isInitialized(order.getItems())) {
                itemsList = order.getItems().stream().map(OrderItemResponse::from).toList();
            }
        } catch (Exception ignored) {
        }

        Long purchaseRequestId = null;
        try {
            if (order.getPurchaseRequest() != null) {
                purchaseRequestId = HibernateUtils.getId(order.getPurchaseRequest());
            }
        } catch (Exception ignored) {
        }

        Long embossFileId = null;
        try {
            if (order.getEmbossFile() != null) {
                embossFileId = HibernateUtils.getId(order.getEmbossFile());
            }
        } catch (Exception ignored) {
        }

        return new OrderResponse(
                id,
                order.getOrderNumber(),
                OrganizationSummary.from(order.getRequestingOrganization()),
                UserSummary.from(order.getCreatedByUser()),
                UserSummary.from(order.getApprovedByUser()),
                order.getRequiredDate(),
                order.getPriority(),
                order.getStatus(),
                order.getOrderType(),
                order.getFulfillmentStatus(),
                purchaseRequestId,
                embossFileId,
                order.getBatchManifestNumber(),
                order.getRoutinePeriod(),
                order.getIsOverbudget(),
                order.getIsPickupKp(),
                order.getDeliveryMethod(),
                order.getTotalEstimatedValue(),
                order.getTotalItems(),
                order.getRejectionReason(),
                order.getNotes(),
                order.getSubmittedAt(),
                order.getApprovedAt(),
                order.getCompletedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemsList
        );
    }

    public record OrganizationSummary(Long id, String name, String code) {
        public static OrganizationSummary from(Organization organization) {
            if (organization == null) {
                return null;
            }
            Long id = HibernateUtils.getId(organization);
            try {
                if (HibernateUtils.isInitialized(organization)) {
                    return new OrganizationSummary(id, organization.getName(), organization.getCode());
                }
            } catch (Exception ignored) {
            }
            return new OrganizationSummary(id, null, null);
        }
    }

    public record UserSummary(Long id, String name, String nip, String email) {
        public static UserSummary from(User user) {
            if (user == null) {
                return null;
            }
            Long id = HibernateUtils.getId(user);
            try {
                if (HibernateUtils.isInitialized(user)) {
                    return new UserSummary(id, user.getName(), user.getNip(), user.getEmail());
                }
            } catch (Exception ignored) {
            }
            return new UserSummary(id, null, null, null);
        }
    }

    public record OrderItemResponse(
            Long id,
            ItemSummary item,
            Integer qtyRequested,
            Integer qtyApproved,
            Integer qtyAllocated,
            Integer qtyPicked,
            Integer qtyPacked,
            Integer qtyShipped,
            Integer qtyReceived,
            BigDecimal unitPriceRef,
            BigDecimal subtotalRef,
            String notes
    ) {
        public static OrderItemResponse from(OrderItem orderItem) {
            if (orderItem == null) {
                return null;
            }
            Long id = HibernateUtils.getId(orderItem);
            try {
                return new OrderItemResponse(
                        id,
                        ItemSummary.from(orderItem.getItem()),
                        orderItem.getQtyRequested(),
                        orderItem.getQtyApproved(),
                        orderItem.getQtyAllocated(),
                        orderItem.getQtyPicked(),
                        orderItem.getQtyPacked(),
                        orderItem.getQtyShipped(),
                        orderItem.getQtyReceived(),
                        orderItem.getUnitPriceRef(),
                        orderItem.getSubtotalRef(),
                        orderItem.getNotes()
                );
            } catch (Exception ignored) {
                return new OrderItemResponse(
                        id,
                        null,
                        null, null, null, null, null, null, null, null, null, null
                );
            }
        }
    }

    public record ItemSummary(Long id, String sku, String name, String uom, BigDecimal estimatedUnitPrice) {
        public static ItemSummary from(Item item) {
            if (item == null) {
                return null;
            }
            Long id = HibernateUtils.getId(item);
            try {
                if (HibernateUtils.isInitialized(item)) {
                    return new ItemSummary(
                            id,
                            item.getSku(),
                            item.getName(),
                            item.getUom(),
                            item.getEstimatedUnitPrice()
                    );
                }
            } catch (Exception ignored) {
            }
            return new ItemSummary(id, null, null, null, null);
        }
    }
}
