package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.BulkOperationDtos;
import com.bankjatim.jims.dto.OrderResponse;
import com.bankjatim.jims.dto.ShipmentResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistributionService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ExpeditionMappingRepository expeditionMappingRepository;
    private final OrganizationRepository organizationRepository;
    private final ItemRepository itemRepository;
    private final WarehousePackingRepository warehousePackingRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final EmbossFileRepository embossFileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(ShipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pengiriman / Manifest tidak ditemukan: " + id));
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse createShipment(Long orderId, Long courierId, String serviceType,
                                           String trackingNumber, BigDecimal shippingCost, User dispatcher) {
        return createShipmentInternal(orderId, courierId, serviceType, trackingNumber, shippingCost, null, dispatcher);
    }

    @Transactional
    public ShipmentResponse createShipmentInternal(Long orderId, Long courierId, String serviceType,
                                                   String trackingNumber, BigDecimal shippingCost,
                                                   String batchManifestNumber, User dispatcher) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));

        Warehouse originWarehouse = warehouseRepository.findFirstByType("CENTRAL_LOGISTICS")
                .orElseGet(() -> warehouseRepository.findAll().stream().findFirst().orElse(null));

        Courier courier = null;
        if (courierId != null) {
            courier = courierRepository.findById(courierId).orElse(null);
        } else {
            ExpeditionMapping mapping = expeditionMappingRepository.findByDestinationOrganizationId(
                    order.getRequestingOrganization().getId()
            ).orElse(null);
            if (mapping != null) {
                courier = mapping.getCourier();
                serviceType = mapping.getServiceType();
            }
        }

        String manifestNumber = "MAN-" + System.currentTimeMillis();
        String effectiveTrackingNumber = (trackingNumber != null && !trackingNumber.isBlank())
                ? trackingNumber
                : "AWB-BJ-" + System.currentTimeMillis();

        int koli = 1;
        BigDecimal weight = new BigDecimal("2.50");
        List<WarehousePacking> packings = warehousePackingRepository.findByOrderId(order.getId());
        if (!packings.isEmpty()) {
            WarehousePacking wp = packings.get(packings.size() - 1);
            if (wp.getKoliCount() != null && wp.getKoliCount() > 0) {
                koli = wp.getKoliCount();
            }
            if (wp.getTotalWeightKg() != null && wp.getTotalWeightKg().compareTo(BigDecimal.ZERO) > 0) {
                weight = wp.getTotalWeightKg();
            }
        }

        String distType = order.getOrderType() != null ? order.getOrderType() : "ORDER_REQUEST";

        Shipment shipment = Shipment.builder()
                .manifestNumber(manifestNumber)
                .distributionType(distType)
                .batchManifestNumber(batchManifestNumber)
                .order(order)
                .originWarehouse(originWarehouse)
                .destinationOrganization(order.getRequestingOrganization())
                .courier(courier)
                .serviceType(serviceType != null ? serviceType : "REGULER")
                .trackingNumber(effectiveTrackingNumber)
                .dispatchedByUser(dispatcher)
                .koliCount(koli)
                .totalWeightKg(weight)
                .shippingCost(shippingCost != null ? shippingCost : BigDecimal.ZERO)
                .etaDate(LocalDate.now().plusDays(2))
                .status("DISPATCHED")
                .dispatchedAt(LocalDateTime.now())
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        int totalApproved = 0;
        int totalShipped = 0;

        for (OrderItem oi : order.getItems()) {
            int toShip = oi.getQtyPacked() > 0 ? oi.getQtyPacked() : oi.getQtyApproved();
            oi.setQtyShipped(toShip);
            totalApproved += (oi.getQtyApproved() != null ? oi.getQtyApproved() : 0);
            totalShipped += toShip;

            ShipmentItem si = ShipmentItem.builder()
                    .shipment(savedShipment)
                    .orderItem(oi)
                    .item(oi.getItem())
                    .qtyShipped(toShip)
                    .qtyReceived(0)
                    .notes("Manifest " + manifestNumber)
                    .build();
            shipmentItemRepository.save(si);
        }

        order.setStatus("IN_TRANSIT");
        if (totalShipped >= totalApproved && totalApproved > 0) {
            order.setFulfillmentStatus("FULLY_FULFILLED");
        } else if (totalShipped > 0) {
            order.setFulfillmentStatus("PARTIALLY_FULFILLED");
        }
        order.setBatchManifestNumber(batchManifestNumber);
        orderRepository.save(order);

        // Synchronize linked Purchase Request fulfillment
        if (order.getPurchaseRequest() != null) {
            PurchaseRequest pr = order.getPurchaseRequest();
            for (PurchaseRequestItem pri : pr.getItems()) {
                pri.setQtyFulfilled(pri.getQtyApproved());
            }
            pr.setFulfillmentStatus(order.getFulfillmentStatus());
            purchaseRequestRepository.save(pr);
        }

        // Synchronize linked Emboss File fulfillment
        if (order.getEmbossFile() != null) {
            EmbossFile ef = order.getEmbossFile();
            ef.setFulfillmentStatus(order.getFulfillmentStatus());
            embossFileRepository.save(ef);
        }

        return ShipmentResponse.from(savedShipment);
    }

    @Transactional
    public List<ShipmentResponse> createBulkShipments(BulkOperationDtos.BulkShipmentRequest request, Long dispatcherUserId) {
        if (request.orderIds() == null || request.orderIds().isEmpty()) {
            return List.of();
        }

        User dispatcher = dispatcherUserId != null ? userRepository.findById(dispatcherUserId).orElse(null) : null;

        String batchNumber = (request.batchManifestNumber() != null && !request.batchManifestNumber().isBlank())
                ? request.batchManifestNumber()
                : "BATCH-MAN-" + System.currentTimeMillis();

        List<ShipmentResponse> results = new ArrayList<>();
        int index = 1;
        for (Long orderId : request.orderIds()) {
            String tracking = (request.trackingNumberPrefix() != null && !request.trackingNumberPrefix().isBlank())
                    ? request.trackingNumberPrefix() + "-" + index
                    : null;

            ShipmentResponse res = createShipmentInternal(
                    orderId,
                    request.courierId(),
                    request.serviceType(),
                    tracking,
                    request.shippingCostPerOrder(),
                    batchNumber,
                    dispatcher
            );
            results.add(res);
            index++;
        }
        return results;
    }

    @Transactional
    public List<OrderResponse> createRoutineDistribution(BulkOperationDtos.RoutineDistributionRequest request, Long creatorUserId) {
        User creator = creatorUserId != null ? userRepository.findById(creatorUserId).orElse(null) : null;
        if (creator == null) {
            creator = userRepository.findAll().stream().findFirst().orElse(null);
        }

        boolean hasBranchAllocations = request.branchAllocations() != null && !request.branchAllocations().isEmpty();
        if (!hasBranchAllocations && (request.organizationIds() == null || request.organizationIds().isEmpty())) {
            throw new RuntimeException("Minimal 1 cabang tujuan harus dipilih");
        }

        Warehouse centralWarehouse = warehouseRepository.findFirstByType("CENTRAL_LOGISTICS")
                .orElseGet(() -> warehouseRepository.findAll().stream().findFirst().orElse(null));

        List<OrderResponse> createdOrders = new ArrayList<>();
        String period = (request.routinePeriod() != null && !request.routinePeriod().isBlank())
                ? request.routinePeriod()
                : "Rutin " + LocalDate.now().getMonth().name() + " " + LocalDate.now().getYear();

        if (hasBranchAllocations) {
            for (BulkOperationDtos.RoutineDistributionRequest.BranchAllocation bAlloc : request.branchAllocations()) {
                if (bAlloc.organizationId() == null) continue;
                if (bAlloc.items() == null || bAlloc.items().isEmpty()) continue;

                Organization org = organizationRepository.findById(bAlloc.organizationId())
                        .orElseThrow(() -> new RuntimeException("Organisasi cabang tidak ditemukan: " + bAlloc.organizationId()));

                Order order = Order.builder()
                        .orderNumber("ORD-ROUTINE-" + org.getCode() + "-" + System.currentTimeMillis() + "-" + (createdOrders.size() + 1))
                        .orderType("ROUTINE_PUSH")
                        .fulfillmentStatus("UNFULFILLED")
                        .routinePeriod(period)
                        .requestingOrganization(org)
                        .requestingWarehouse(centralWarehouse)
                        .createdByUser(creator)
                        .approvedByUser(creator)
                        .approvedAt(LocalDateTime.now())
                        .submittedAt(LocalDateTime.now())
                        .priority("NORMAL")
                        .requiredDate(LocalDate.now().plusDays(3))
                        .status("APPROVED") // Langsung siap ambil di gudang (Picking queue)
                        .notes(request.notes() != null ? request.notes() : "Drop Kuota Rutin Periode " + period)
                        .totalItems(0)
                        .totalEstimatedValue(BigDecimal.ZERO)
                        .build();

                int totalItems = 0;
                BigDecimal totalValue = BigDecimal.ZERO;

                for (BulkOperationDtos.RoutineDistributionRequest.RoutineItem rItem : bAlloc.items()) {
                    if (rItem.itemId() == null || rItem.qty() <= 0) continue;
                    Item item = itemRepository.findById(rItem.itemId())
                            .orElseThrow(() -> new RuntimeException("Item barang tidak ditemukan: " + rItem.itemId()));

                    BigDecimal price = item.getEstimatedUnitPrice() != null ? item.getEstimatedUnitPrice() : BigDecimal.ZERO;
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(rItem.qty()));

                    OrderItem oi = OrderItem.builder()
                            .order(order)
                            .item(item)
                            .qtyRequested(rItem.qty())
                            .qtyApproved(rItem.qty())
                            .qtyAllocated(rItem.qty())
                            .qtyPicked(0)
                            .qtyPacked(0)
                            .qtyShipped(0)
                            .qtyReceived(0)
                            .unitPriceRef(price)
                            .subtotalRef(subtotal)
                            .notes("Alokasi Rutin " + period)
                            .build();

                    order.getItems().add(oi);
                    totalItems += rItem.qty();
                    totalValue = totalValue.add(subtotal);
                }

                if (totalItems > 0) {
                    order.setTotalItems(totalItems);
                    order.setTotalEstimatedValue(totalValue);

                    Order saved = orderRepository.save(order);
                    createdOrders.add(OrderResponse.from(saved));
                }
            }
        } else {
            if (request.items() == null || request.items().isEmpty()) {
                throw new RuntimeException("Minimal 1 item barang harus ditentukan kuotanya");
            }

            for (Long orgId : request.organizationIds()) {
                Organization org = organizationRepository.findById(orgId)
                        .orElseThrow(() -> new RuntimeException("Organisasi cabang tidak ditemukan: " + orgId));

                Order order = Order.builder()
                        .orderNumber("ORD-ROUTINE-" + org.getCode() + "-" + System.currentTimeMillis() + "-" + (createdOrders.size() + 1))
                        .orderType("ROUTINE_PUSH")
                        .fulfillmentStatus("UNFULFILLED")
                        .routinePeriod(period)
                        .requestingOrganization(org)
                        .requestingWarehouse(centralWarehouse)
                        .createdByUser(creator)
                        .approvedByUser(creator)
                        .approvedAt(LocalDateTime.now())
                        .submittedAt(LocalDateTime.now())
                        .priority("NORMAL")
                        .requiredDate(LocalDate.now().plusDays(3))
                        .status("APPROVED") // Langsung siap ambil di gudang (Picking queue)
                        .notes(request.notes() != null ? request.notes() : "Drop Kuota Rutin Periode " + period)
                        .totalItems(0)
                        .totalEstimatedValue(BigDecimal.ZERO)
                        .build();

                int totalItems = 0;
                BigDecimal totalValue = BigDecimal.ZERO;

                for (BulkOperationDtos.RoutineDistributionRequest.RoutineItem rItem : request.items()) {
                    if (rItem.qty() <= 0) continue;
                    Item item = itemRepository.findById(rItem.itemId())
                            .orElseThrow(() -> new RuntimeException("Item barang tidak ditemukan: " + rItem.itemId()));

                    BigDecimal price = item.getEstimatedUnitPrice() != null ? item.getEstimatedUnitPrice() : BigDecimal.ZERO;
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(rItem.qty()));

                    OrderItem oi = OrderItem.builder()
                            .order(order)
                            .item(item)
                            .qtyRequested(rItem.qty())
                            .qtyApproved(rItem.qty()) // Approved by Central
                            .qtyAllocated(rItem.qty())
                            .qtyPicked(0)
                            .qtyPacked(0)
                            .qtyShipped(0)
                            .qtyReceived(0)
                            .unitPriceRef(price)
                            .subtotalRef(subtotal)
                            .notes("Alokasi Rutin " + period)
                            .build();

                    order.getItems().add(oi);
                    totalItems += rItem.qty();
                    totalValue = totalValue.add(subtotal);
                }

                if (totalItems > 0) {
                    order.setTotalItems(totalItems);
                    order.setTotalEstimatedValue(totalValue);

                    Order saved = orderRepository.save(order);
                    createdOrders.add(OrderResponse.from(saved));
                }
            }
        }

        return createdOrders;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRoutineDistributions() {
        return orderRepository.findRoutineDistributionsWithDetails().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
