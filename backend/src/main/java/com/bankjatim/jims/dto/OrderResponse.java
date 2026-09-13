package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.Order;
import com.bankjatim.jims.domain.OrderItem;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                OrganizationSummary.from(order.getRequestingOrganization()),
                UserSummary.from(order.getCreatedByUser()),
                UserSummary.from(order.getApprovedByUser()),
                order.getRequiredDate(),
                order.getPriority(),
                order.getStatus(),
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
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }

    public record OrganizationSummary(Long id, String name, String code) {
        static OrganizationSummary from(Organization organization) {
            if (organization == null) {
                return null;
            }
            return new OrganizationSummary(organization.getId(), organization.getName(), organization.getCode());
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
        static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getId(),
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
        }
    }

    public record ItemSummary(Long id, String sku, String name, String uom, BigDecimal estimatedUnitPrice) {
        static ItemSummary from(Item item) {
            if (item == null) {
                return null;
            }
            return new ItemSummary(
                    item.getId(),
                    item.getSku(),
                    item.getName(),
                    item.getUom(),
                    item.getEstimatedUnitPrice()
            );
        }
    }
}
