package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.OrderRequest;
import com.bankjatim.jims.dto.OrderResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final WarehouseRepository warehouseRepository;
    private final BudgetRepository budgetRepository;
    private final OrderAllocationRepository orderAllocationRepository;
    private final OrganizationRepository organizationRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Long organizationId, Pageable pageable) {
        Page<Order> orders = (organizationId != null)
                ? orderRepository.findAllWithDetails(organizationId, pageable)
                : orderRepository.findAllWithDetails(pageable);
        return orders.map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public List<Order> getPendingApprovals() {
        return orderRepository.findPendingApprovals();
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, User creator) {
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new RuntimeException("Organisasi tidak ditemukan: " + request.organizationId()));

        Order order = Order.builder()
                .orderNumber("ORD-" + System.currentTimeMillis())
                .requestingOrganization(organization)
                .requestingWarehouse(creator.getWarehouse())
                .createdByUser(creator)
                .priority(request.priority() != null ? request.priority() : "NORMAL")
                .requiredDate(request.requiredDate())
                .status("SUBMITTED")
                .notes(request.notes())
                .submittedAt(LocalDateTime.now())
                .totalItems(0)
                .totalEstimatedValue(BigDecimal.ZERO)
                .build();

        BigDecimal totalEstimatedValue = BigDecimal.ZERO;
        int totalItems = 0;
        for (OrderRequest.ItemRequest itemRequest : request.items()) {
            if (itemRequest.qty() == null || itemRequest.qty() <= 0) {
                throw new RuntimeException("Kuantitas order harus lebih dari 0");
            }

            Item item = itemRepository.findById(itemRequest.itemId())
                    .orElseThrow(() -> new RuntimeException("Barang tidak ditemukan: " + itemRequest.itemId()));
            BigDecimal unitPrice = item.getEstimatedUnitPrice() != null ? item.getEstimatedUnitPrice() : BigDecimal.ZERO;
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.qty()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .qtyRequested(itemRequest.qty())
                    .unitPriceRef(unitPrice)
                    .subtotalRef(subtotal)
                    .build();

            order.getItems().add(orderItem);
            totalItems += itemRequest.qty();
            totalEstimatedValue = totalEstimatedValue.add(subtotal);
        }

        BigDecimal remainingBudget = budgetRepository.findByOrganizationIdAndFiscalYear(
                        organization.getId(), Year.now().getValue()).stream()
                .map(budget -> budget.getAllocatedAmount()
                        .subtract(budget.getCommittedAmount())
                        .subtract(budget.getRealizedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalItems(totalItems);
        order.setTotalEstimatedValue(totalEstimatedValue);
        order.setIsOverbudget(remainingBudget.compareTo(BigDecimal.ZERO) > 0
                && totalEstimatedValue.compareTo(remainingBudget) > 0);

        Order saved = orderRepository.save(order);
        return OrderResponse.from(orderRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Transactional
    public Order approveOrder(Long orderId, User approver) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));

        order.setStatus("APPROVED");
        order.setApprovedByUser(approver);
        order.setApprovedAt(LocalDateTime.now());

        // Reserve stock in central warehouse
        Warehouse centralWarehouse = warehouseRepository.findByType("CENTRAL_LOGISTICS")
                .orElseGet(() -> warehouseRepository.findAll().get(0));

        for (OrderItem oi : order.getItems()) {
            oi.setQtyApproved(oi.getQtyRequested());

            StockBalance balance = stockBalanceRepository.findByWarehouseIdAndItemId(
                    centralWarehouse.getId(), oi.getItem().getId()
            ).orElse(null);

            if (balance != null) {
                balance.setReserved(balance.getReserved() + oi.getQtyApproved());
                stockBalanceRepository.save(balance);

                OrderAllocation alloc = OrderAllocation.builder()
                        .orderItem(oi)
                        .sourceWarehouse(centralWarehouse)
                        .qtyAllocated(oi.getQtyApproved())
                        .allocationType("DIRECT_WAREHOUSE")
                        .status("RESERVED")
                        .build();

                orderAllocationRepository.save(alloc);
            }
        }

        Order saved = orderRepository.save(order);
        return orderRepository.findByIdWithDetails(saved.getId()).orElse(saved);
    }
}
