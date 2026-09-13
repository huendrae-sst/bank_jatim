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
    public List<WarehouseQueueResponse> getPickingQueue() {
        return orderRepository.findApprovedOrdersQueue().stream()
                .map(WarehouseQueueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseQueueResponse> getPackingQueue() {
        return orderRepository.findWarehouseProcessingOrders().stream()
                .map(WarehouseQueueResponse::from)
                .toList();
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
}
