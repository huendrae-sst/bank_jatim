package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.WarehousePackingResponse;
import com.bankjatim.jims.dto.WarehousePickingResponse;
import com.bankjatim.jims.dto.WarehouseQueueResponse;
import com.bankjatim.jims.repository.OrderRepository;
import com.bankjatim.jims.repository.WarehousePackingRepository;
import com.bankjatim.jims.repository.WarehousePickingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final OrderRepository orderRepository;
    private final WarehousePickingRepository pickingRepository;
    private final WarehousePackingRepository packingRepository;

    @Transactional(readOnly = true)
    public List<WarehouseQueueResponse> getPickingQueue(String orderType) {
        String normalized = (orderType != null && !orderType.isBlank() && !"ALL".equalsIgnoreCase(orderType)) ? orderType : null;
        return orderRepository.findApprovedOrdersQueue(normalized).stream()
                .map(WarehouseQueueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseQueueResponse> getPickingQueue() {
        return getPickingQueue(null);
    }

    @Transactional(readOnly = true)
    public List<WarehouseQueueResponse> getPackingQueue(String orderType) {
        String normalized = (orderType != null && !orderType.isBlank() && !"ALL".equalsIgnoreCase(orderType)) ? orderType : null;
        return orderRepository.findWarehouseProcessingOrders(normalized).stream()
                .map(WarehouseQueueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseQueueResponse> getPackingQueue() {
        return getPackingQueue(null);
    }

    @Transactional
    public WarehousePickingResponse completePicking(Long orderId, User picker) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));

        WarehousePicking picking = WarehousePicking.builder()
                .pickingNumber("PCK-" + System.currentTimeMillis())
                .order(order)
                .warehouse(order.getRequestingWarehouse())
                .pickedByUser(picker)
                .status("COMPLETED")
                .pickedAt(LocalDateTime.now())
                .build();

        for (OrderItem oi : order.getItems()) {
            oi.setQtyPicked(oi.getQtyApproved());
        }
        order.setStatus("PICKING");
        orderRepository.save(order);

        return WarehousePickingResponse.from(pickingRepository.save(picking));
    }

    @Transactional
    public WarehousePackingResponse completePacking(Long orderId, int koliCount, BigDecimal totalWeightKg,
                                           String dimensionsCm, User packer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));

        WarehousePacking packing = WarehousePacking.builder()
                .packingNumber("PCK-BOX-" + System.currentTimeMillis())
                .order(order)
                .warehouse(order.getRequestingWarehouse())
                .packedByUser(packer)
                .koliCount(koliCount)
                .totalWeightKg(totalWeightKg)
                .dimensionsCm(dimensionsCm)
                .status("PACKED")
                .packedAt(LocalDateTime.now())
                .build();

        for (OrderItem oi : order.getItems()) {
            oi.setQtyPacked(oi.getQtyPicked());
        }
        order.setStatus("READY_TO_SHIP");
        orderRepository.save(order);

        return WarehousePackingResponse.from(packingRepository.save(packing));
    }

    @Transactional
    public List<WarehousePickingResponse> completeBatchPicking(List<Long> orderIds, User picker) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return orderIds.stream()
                .map(id -> completePicking(id, picker))
                .toList();
    }

    @Transactional
    public List<WarehousePackingResponse> completeBatchPacking(
            List<com.bankjatim.jims.dto.BulkOperationDtos.BatchPackingRequest.PackingItem> items, User packer) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .map(item -> completePacking(
                        item.orderId(),
                        item.koliCount() > 0 ? item.koliCount() : 1,
                        item.totalWeightKg() != null ? item.totalWeightKg() : new BigDecimal("1.00"),
                        item.dimensionsCm() != null && !item.dimensionsCm().isBlank() ? item.dimensionsCm() : "30x20x15",
                        packer
                ))
                .toList();
    }
}
