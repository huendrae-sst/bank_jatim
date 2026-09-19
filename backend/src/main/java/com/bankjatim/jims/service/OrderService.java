package com.bankjatim.jims.service;

import com.bankjatim.jims.common.BadRequestException;
import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.OrderRequest;
import com.bankjatim.jims.dto.OrderResponse;
import com.bankjatim.jims.repository.*;
import com.bankjatim.jims.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
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
    private final UserRepository userRepository;

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
    public Page<OrderResponse> getOrders(UserPrincipal principal, Long requestedOrgId, Pageable pageable) {
        if (principal == null || "SUPER_ADMIN".equalsIgnoreCase(principal.getRole()) || "MANAGEMENT".equalsIgnoreCase(principal.getRole())) {
            return getOrders(requestedOrgId, pageable);
        }

        if ("REGIONAL_MONITOR".equalsIgnoreCase(principal.getRole())) {
            Long regionId = principal.getRegionId();
            if (regionId == null) {
                return Page.empty(pageable);
            }
            List<Long> affiliatedOrgIds = organizationRepository.findAffiliatedOrgIdsByRegionId(regionId);
            if (affiliatedOrgIds.isEmpty()) {
                return Page.empty(pageable);
            }
            if (requestedOrgId != null) {
                if (!affiliatedOrgIds.contains(requestedOrgId)) {
                    throw new BadRequestException("Cabang yang dipilih tidak berada di bawah wilayah Anda");
                }
                return orderRepository.findAllWithDetails(requestedOrgId, pageable).map(OrderResponse::from);
            }
            return orderRepository.findAllByOrganizationIdsIn(affiliatedOrgIds, pageable).map(OrderResponse::from);
        }

        Long orgId = principal.getOrganizationId() != null ? principal.getOrganizationId() : requestedOrgId;
        return getOrders(orgId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Order> getPendingApprovals() {
        return orderRepository.findPendingApprovals();
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, User creator) {
        if (creator != null && creator.getId() != null) {
            creator = userRepository.findById(creator.getId()).orElse(creator);
        }

        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new BadRequestException("Organisasi tidak ditemukan: " + request.organizationId()));

        Warehouse requestingWarehouse = (creator != null) ? creator.getWarehouse() : null;
        if (requestingWarehouse == null && organization != null) {
            List<Warehouse> orgWarehouses = warehouseRepository.findByOrganizationId(organization.getId());
            if (!orgWarehouses.isEmpty()) {
                requestingWarehouse = orgWarehouses.get(0);
            }
        }

        Order order = Order.builder()
                .orderNumber("ORD-" + System.currentTimeMillis())
                .requestingOrganization(organization)
                .requestingWarehouse(requestingWarehouse)
                .createdByUser(creator)
                .priority(request.priority() != null ? request.priority() : "NORMAL")
                .requiredDate(request.requiredDate() != null ? request.requiredDate() : LocalDate.now().plusDays(3))
                .status("SUBMITTED")
                .notes(request.notes())
                .submittedAt(LocalDateTime.now())
                .totalItems(0)
                .totalEstimatedValue(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalEstimatedValue = BigDecimal.ZERO;
        int totalItems = 0;
        for (OrderRequest.ItemRequest itemRequest : request.items()) {
            if (itemRequest.qty() == null || itemRequest.qty() <= 0) {
                throw new BadRequestException("Kuantitas order harus lebih dari 0");
            }

            Item item = itemRepository.findById(itemRequest.itemId())
                    .orElseThrow(() -> new BadRequestException("Barang tidak ditemukan: " + itemRequest.itemId()));
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

        Order saved = orderRepository.saveAndFlush(order);
        return OrderResponse.from(orderRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Transactional
    public Order approveOrder(Long orderId, User approver) {
        if (approver != null && approver.getId() != null) {
            approver = userRepository.findById(approver.getId()).orElse(approver);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));

        order.setStatus("APPROVED");
        order.setFulfillmentStatus("UNFULFILLED");
        order.setApprovedByUser(approver);
        order.setApprovedAt(LocalDateTime.now());

        // Reserve stock in central warehouse
        Warehouse centralWarehouse = warehouseRepository.findFirstByType("CENTRAL_LOGISTICS")
                .orElseGet(() -> warehouseRepository.findAll().stream().findFirst().orElse(null));

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

        Order saved = orderRepository.saveAndFlush(order);
        return orderRepository.findByIdWithDetails(saved.getId()).orElse(saved);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));
        if ("APPROVED".equalsIgnoreCase(order.getStatus()) ||
            "PRODUCTION".equalsIgnoreCase(order.getStatus()) ||
            "IN_PRODUCTION".equalsIgnoreCase(order.getStatus()) ||
            "PRODUKSI".equalsIgnoreCase(order.getStatus()) ||
            "ALLOCATED".equalsIgnoreCase(order.getStatus()) ||
            "PICKING".equalsIgnoreCase(order.getStatus()) ||
            "PACKING".equalsIgnoreCase(order.getStatus()) ||
            "READY_TO_SHIP".equalsIgnoreCase(order.getStatus()) ||
            "IN_TRANSIT".equalsIgnoreCase(order.getStatus()) ||
            "RECEIVED".equalsIgnoreCase(order.getStatus()) ||
            "COMPLETED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Order yang sudah disetujui tidak dapat dihapus");
        }
        orderRepository.delete(order);
    }
}
