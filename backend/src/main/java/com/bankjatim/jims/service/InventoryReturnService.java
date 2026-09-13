package com.bankjatim.jims.service;

import com.bankjatim.jims.common.ResourceNotFoundException;
import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.InventoryReturnRequest;
import com.bankjatim.jims.dto.InventoryReturnResponse;
import com.bankjatim.jims.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReturnService {

    private final InventoryReturnRepository returnRepository;
    private final OrganizationRepository organizationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<InventoryReturnResponse> getAllReturns(String status) {
        List<InventoryReturn> list;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = returnRepository.findByStatusOrderByIdDesc(status);
        } else {
            list = returnRepository.findAllWithDetails();
        }
        return list.stream().map(InventoryReturnResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public InventoryReturnResponse getReturnById(Long id) {
        InventoryReturn r = returnRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan retur #" + id + " tidak ditemukan"));
        return InventoryReturnResponse.from(r);
    }

    @Transactional
    public InventoryReturnResponse createReturn(InventoryReturnRequest request, User user) {
        Organization org = user.getOrganization();
        if (org == null && request.getOrganizationId() != null) {
            org = organizationRepository.findById(request.getOrganizationId()).orElse(null);
        }
        if (org == null) {
            org = organizationRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Organization tidak tersedia"));
        }

        Warehouse destWarehouse = warehouseRepository.findById(request.getDestinationWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Gudang tujuan #" + request.getDestinationWarehouseId() + " tidak ditemukan"));

        String returnNumber = "RET-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", System.currentTimeMillis() % 10000);

        InventoryReturn ret = InventoryReturn.builder()
                .returnNumber(returnNumber)
                .organization(org)
                .destinationWarehouse(destWarehouse)
                .createdByUser(user)
                .reason(request.getReason() + (request.getReasonDetails() != null ? " - " + request.getReasonDetails() : ""))
                .status("REQUESTED")
                .build();

        for (InventoryReturnRequest.ItemLine line : request.getItems()) {
            Item item = itemRepository.findById(line.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barang #" + line.getItemId() + " tidak ditemukan"));

            InventoryReturnItem retItem = InventoryReturnItem.builder()
                    .item(item)
                    .qtyReturned(line.getQty())
                    .qtyReceived(0)
                    .condition(line.getCondition() != null ? line.getCondition() : "DAMAGED")
                    .notes(line.getNotes())
                    .build();

            ret.addItem(retItem);
        }

        InventoryReturn saved = returnRepository.save(ret);
        return InventoryReturnResponse.from(saved);
    }

    @Transactional
    public InventoryReturnResponse approveReturn(Long id, User user) {
        InventoryReturn ret = returnRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan retur #" + id + " tidak ditemukan"));

        ret.setStatus("APPROVED");
        ret.setApprovedByUser(user);
        return InventoryReturnResponse.from(returnRepository.save(ret));
    }

    @Transactional
    public InventoryReturnResponse rejectReturn(Long id, String reason, User user) {
        InventoryReturn ret = returnRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan retur #" + id + " tidak ditemukan"));

        ret.setStatus("REJECTED");
        ret.setApprovedByUser(user);
        if (reason != null && !reason.isBlank()) {
            ret.setReason(ret.getReason() + " [DITOLAK: " + reason + "]");
        }
        return InventoryReturnResponse.from(returnRepository.save(ret));
    }

    @Transactional
    public InventoryReturnResponse shipReturn(Long id, String courierName, String trackingNumber) {
        InventoryReturn ret = returnRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan retur #" + id + " tidak ditemukan"));

        ret.setStatus("SHIPPED");
        ret.setShippedAt(LocalDateTime.now());
        if (trackingNumber != null && !trackingNumber.isBlank()) {
            ret.setReason(ret.getReason() + " [RESI: " + trackingNumber + " via " + (courierName != null ? courierName : "-") + "]");
        }
        return InventoryReturnResponse.from(returnRepository.save(ret));
    }

    @Transactional
    public InventoryReturnResponse receiveReturn(Long id, List<ReceiveLine> lines, User user) {
        InventoryReturn ret = returnRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan retur #" + id + " tidak ditemukan"));

        ret.setStatus("RECEIVED");
        ret.setReceivedAt(LocalDateTime.now());

        if (lines != null && !lines.isEmpty()) {
            for (ReceiveLine line : lines) {
                for (InventoryReturnItem item : ret.getItems()) {
                    if (item.getId().equals(line.itemId) || (item.getItem() != null && item.getItem().getId().equals(line.itemId))) {
                        item.setQtyReceived(line.qtyReceivedGood);
                        // Record stock movement back to warehouse
                        if (line.qtyReceivedGood > 0 && ret.getDestinationWarehouse() != null) {
                            inventoryService.recordStockMovement(
                                    ret.getDestinationWarehouse().getId(),
                                    item.getItem().getId(),
                                    "RETURN_IN",
                                    ret.getReturnNumber(),
                                    line.qtyReceivedGood,
                                    0,
                                    item.getItem().getEstimatedUnitPrice(),
                                    "Penerimaan retur barang dari " + ret.getOrganization().getName(),
                                    user
                            );
                        }
                    }
                }
            }
        }

        return InventoryReturnResponse.from(returnRepository.save(ret));
    }

    public record ReceiveLine(Long itemId, Integer qtyReceivedGood, Integer qtyReceivedDamaged) {}
}
