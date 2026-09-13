package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.DiscrepancyResponse;
import com.bankjatim.jims.dto.ReceivingResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceivingService {

    private final ReceivingRepository receivingRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final DiscrepancyRepository discrepancyRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<ReceivingResponse> getReceivings(Long organizationId) {
        List<Receiving> receivings = organizationId != null
                ? receivingRepository.findByOrganizationId(organizationId)
                : receivingRepository.findAll();
        return receivings.stream().map(ReceivingResponse::from).toList();
    }

    @Transactional
    public ReceivingResponse confirmReceipt(Long shipmentId, String podSignature, String notes,
                                   List<Discrepancy> reportedDiscrepancies, User receiver) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Pengiriman tidak ditemukan: " + shipmentId));

        Order order = shipment.getOrder();

        String receivingNumber = "RCV-" + System.currentTimeMillis();
        boolean hasDiscrepancy = reportedDiscrepancies != null && !reportedDiscrepancies.isEmpty();

        Receiving receiving = Receiving.builder()
                .receivingNumber(receivingNumber)
                .shipment(shipment)
                .order(order)
                .organization(shipment.getDestinationOrganization())
                .warehouse(order != null ? order.getRequestingWarehouse() : null)
                .receivedByUser(receiver)
                .receiptDate(LocalDate.now())
                .status(hasDiscrepancy ? "DISCREPANCY" : "RECEIVED_FULL")
                .podSignature(podSignature)
                .notes(notes)
                .build();

        Receiving savedReceiving = receivingRepository.save(receiving);

        if (hasDiscrepancy) {
            for (Discrepancy disc : reportedDiscrepancies) {
                disc.setReceiving(savedReceiving);
                disc.setBeritaAcaraNumber("BAP-" + System.currentTimeMillis());
                discrepancyRepository.save(disc);
            }
        }

        // Update physical stock balance at receiving branch warehouse
        if (order != null) {
            for (OrderItem oi : order.getItems()) {
                oi.setQtyReceived(oi.getQtyShipped());
                if (order.getRequestingWarehouse() != null) {
                    inventoryService.recordStockMovement(
                            order.getRequestingWarehouse().getId(),
                            oi.getItem().getId(),
                            "GOODS_RECEIPT_UNIT",
                            receivingNumber,
                            oi.getQtyReceived(),
                            0,
                            oi.getUnitPriceRef(),
                            "Penerimaan Kiriman Resi " + shipment.getTrackingNumber(),
                            receiver
                    );
                }
            }
            order.setStatus(hasDiscrepancy ? "DISCREPANCY" : "COMPLETED");
            orderRepository.save(order);
        }

        shipment.setStatus("DELIVERED");
        shipmentRepository.save(shipment);

        return ReceivingResponse.from(savedReceiving);
    }

    @Transactional(readOnly = true)
    public List<DiscrepancyResponse> getDiscrepancies() {
        return discrepancyRepository.findAll().stream()
                .map(DiscrepancyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DiscrepancyResponse getDiscrepancyById(Long id) {
        Discrepancy disc = discrepancyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discrepancy tidak ditemukan: " + id));
        return DiscrepancyResponse.from(disc);
    }
}
