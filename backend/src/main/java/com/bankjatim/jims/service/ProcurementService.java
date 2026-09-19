package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.GoodsReceiptResponse;
import com.bankjatim.jims.dto.PurchaseOrderResponse;
import com.bankjatim.jims.dto.PurchaseRequestResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcurementService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final VendorRepository vendorRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Page<PurchaseRequestResponse> getPurchaseRequests(Long organizationId, Pageable pageable) {
        Page<PurchaseRequest> page = organizationId != null
                ? purchaseRequestRepository.findByOrganizationId(organizationId, pageable)
                : purchaseRequestRepository.findAll(pageable);
        return page.map(PurchaseRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public List<PurchaseRequestResponse> getApprovedPrPool() {
        return purchaseRequestRepository.findApprovedPool().stream()
                .map(PurchaseRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrders() {
        return purchaseOrderRepository.findAllWithDetails().stream()
                .map(PurchaseOrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseRequestResponse getPurchaseRequestById(Long id) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PR tidak ditemukan: " + id));
        return PurchaseRequestResponse.from(pr);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PO tidak ditemukan: " + id));
        return PurchaseOrderResponse.from(po);
    }

    @Transactional
    public PurchaseRequestResponse rejectPR(Long prId, String reason, User user) {
        PurchaseRequest pr = purchaseRequestRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR tidak ditemukan: " + prId));
        pr.setStatus("REJECTED");
        pr.setRejectionReason(reason);
        pr.setApprovedByUser(user);
        pr.setApprovedAt(LocalDateTime.now());
        return PurchaseRequestResponse.from(purchaseRequestRepository.save(pr));
    }

    @Transactional
    public PurchaseRequestResponse approvePR(Long prId, User approver) {
        PurchaseRequest pr = purchaseRequestRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR tidak ditemukan: " + prId));

        pr.setStatus("APPROVED");
        pr.setApprovedByUser(approver);
        pr.setApprovedAt(LocalDateTime.now());
        for (PurchaseRequestItem item : pr.getItems()) {
            item.setQtyApproved(item.getQtyRequested());
        }

        return PurchaseRequestResponse.from(purchaseRequestRepository.save(pr));
    }

    @Transactional
    public PurchaseOrderResponse consolidatePrsToPo(Long vendorId, Long warehouseId, List<Long> prItemIds, User user) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor tidak ditemukan: " + vendorId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Gudang tujuan tidak ditemukan: " + warehouseId));

        String poNumber = "PO-" + System.currentTimeMillis();
        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNumber)
                .vendor(vendor)
                .warehouse(warehouse)
                .createdByUser(user)
                .orderDate(LocalDate.now())
                .status("ISSUED")
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseRequest pr : purchaseRequestRepository.findApprovedPool()) {
            for (PurchaseRequestItem pri : pr.getItems()) {
                if (prItemIds.contains(pri.getId())) {
                    int qty = pri.getQtyApproved() - pri.getQtyOrdered();
                    if (qty > 0) {
                        BigDecimal itemTotal = pri.getEstimatedUnitPrice().multiply(BigDecimal.valueOf(qty));
                        subtotal = subtotal.add(itemTotal);

                        PurchaseOrderItem poi = PurchaseOrderItem.builder()
                                .purchaseOrder(po)
                                .purchaseRequestItem(pri)
                                .item(pri.getItem())
                                .qtyOrdered(qty)
                                .qtyReceived(0)
                                .unitPrice(pri.getEstimatedUnitPrice())
                                .subtotal(itemTotal)
                                .build();

                        po.getItems().add(poi);
                        pri.setQtyOrdered(pri.getQtyOrdered() + qty);
                    }
                }
            }
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.11")); // PPN 11%
        po.setSubtotal(subtotal);
        po.setTaxAmount(tax);
        po.setTotalAmount(subtotal.add(tax));

        return PurchaseOrderResponse.from(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse approvePO(Long poId, User approver) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("PO tidak ditemukan: " + poId));

        po.setStatus("APPROVED");
        po.setApprovedByUser(approver);
        po.setRejectionReason(null);

        return PurchaseOrderResponse.from(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse rejectPO(Long poId, String reason, User approver) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("PO tidak ditemukan: " + poId));

        po.setStatus("REJECTED");
        po.setApprovedByUser(approver);
        po.setRejectionReason(reason);

        return PurchaseOrderResponse.from(purchaseOrderRepository.save(po));
    }

    @Transactional
    public GoodsReceiptResponse receiveGoodsFromVendor(Long poId, String deliveryNoteNumber, User receiver) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("PO tidak ditemukan: " + poId));

        String grnNumber = "GRN-" + System.currentTimeMillis();
        GoodsReceipt grn = GoodsReceipt.builder()
                .grnNumber(grnNumber)
                .purchaseOrder(po)
                .warehouse(po.getWarehouse())
                .receivedByUser(receiver)
                .vendorDeliveryNoteNumber(deliveryNoteNumber)
                .receiptDate(LocalDate.now())
                .status("RECEIVED")
                .build();

        for (PurchaseOrderItem poi : po.getItems()) {
            GoodsReceiptItem gri = GoodsReceiptItem.builder()
                    .goodsReceipt(grn)
                    .purchaseOrderItem(poi)
                    .item(poi.getItem())
                    .qtyReceived(poi.getQtyOrdered())
                    .qtyAccepted(poi.getQtyOrdered())
                    .qtyRejected(0)
                    .build();

            grn.getItems().add(gri);
            poi.setQtyReceived(poi.getQtyOrdered());

            // Update physical stock balance and write immutable ledger
            inventoryService.recordStockMovement(
                    po.getWarehouse().getId(),
                    poi.getItem().getId(),
                    "PROCUREMENT_RECEIPT",
                    grnNumber,
                    poi.getQtyOrdered(),
                    0,
                    poi.getUnitPrice(),
                    "Penerimaan Vendor PO " + po.getPoNumber(),
                    receiver
            );
        }

        po.setStatus("RECEIVED");
        purchaseOrderRepository.save(po);
        return GoodsReceiptResponse.from(goodsReceiptRepository.save(grn));
    }

    @Transactional
    public com.bankjatim.jims.dto.OrderResponse dispatchPrToBranchDistribution(Long prId, User dispatcher) {
        PurchaseRequest pr = purchaseRequestRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("PR tidak ditemukan: " + prId));

        Order order = Order.builder()
                .orderNumber("ORD-PR-" + pr.getOrganization().getCode() + "-" + System.currentTimeMillis())
                .orderType("PURCHASE_REQUEST")
                .fulfillmentStatus("UNFULFILLED")
                .purchaseRequest(pr)
                .requestingWarehouse(warehouseRepository.findByOrganizationId(pr.getOrganization().getId()).stream().findFirst().orElse(null))
                .createdByUser(dispatcher)
                .approvedByUser(dispatcher)
                .approvedAt(LocalDateTime.now())
                .submittedAt(LocalDateTime.now())
                .priority("HIGH")
                .requiredDate(LocalDate.now().plusDays(2))
                .status("APPROVED") // Siap di antrean picking gudang
                .notes("Pemenuhan Pengadaan PR: " + pr.getPrNumber() + " (" + pr.getPurpose() + ")")
                .totalItems(0)
                .totalEstimatedValue(BigDecimal.ZERO)
                .build();

        int totalItems = 0;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (PurchaseRequestItem pri : pr.getItems()) {
            int qtyToFulfill = pri.getQtyApproved() > 0 ? pri.getQtyApproved() : pri.getQtyRequested();
            BigDecimal price = pri.getEstimatedUnitPrice() != null ? pri.getEstimatedUnitPrice() : BigDecimal.ZERO;
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qtyToFulfill));

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .item(pri.getItem())
                    .qtyRequested(qtyToFulfill)
                    .qtyApproved(qtyToFulfill)
                    .qtyAllocated(qtyToFulfill)
                    .qtyPicked(0)
                    .qtyPacked(0)
                    .qtyShipped(0)
                    .qtyReceived(0)
                    .unitPriceRef(price)
                    .subtotalRef(subtotal)
                    .notes("Pemenuhan PR Item " + pri.getItem().getName())
                    .build();

            order.getItems().add(oi);
            totalItems += qtyToFulfill;
            totalValue = totalValue.add(subtotal);
        }

        order.setTotalItems(totalItems);
        order.setTotalEstimatedValue(totalValue);

        Order saved = orderRepository.save(order);

        pr.setStatus("IN_FULFILLMENT");
        pr.setFulfillmentStatus("UNFULFILLED");
        purchaseRequestRepository.save(pr);

        return com.bankjatim.jims.dto.OrderResponse.from(orderRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Transactional(readOnly = true)
    public List<com.bankjatim.jims.dto.OrderResponse> getPrFulfillmentOrders() {
        return orderRepository.findByOrderTypeOrderByCreatedAtDesc("PURCHASE_REQUEST").stream()
                .map(com.bankjatim.jims.dto.OrderResponse::from)
                .toList();
    }
}
