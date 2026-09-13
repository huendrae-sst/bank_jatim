package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.Order;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.SwitchingStock;
import com.bankjatim.jims.domain.SwitchingStockItem;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.domain.Warehouse;
import com.bankjatim.jims.dto.SwitchingStockRequest;
import com.bankjatim.jims.dto.SwitchingStockResponse;
import com.bankjatim.jims.repository.ItemRepository;
import com.bankjatim.jims.repository.OrderRepository;
import com.bankjatim.jims.repository.OrganizationRepository;
import com.bankjatim.jims.repository.SwitchingStockRepository;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SwitchingStockService {

    private final SwitchingStockRepository switchingStockRepository;
    private final OrganizationRepository organizationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SwitchingStockResponse> getSwitchingStocks(String status) {
        String normalizedStatus = status != null && !status.isBlank() ? status : null;
        return switchingStockRepository.findAllWithDetails(normalizedStatus).stream()
                .map(SwitchingStockResponse::from)
                .toList();
    }

    @Transactional
    public SwitchingStockResponse createSwitching(SwitchingStockRequest request, Long userId) {
        SwitchingStock switchingStock = SwitchingStock.builder()
                .order(loadOrder(request.orderId()))
                .sourceOrganization(loadOrganization(request.sourceOrganizationId()))
                .sourceWarehouse(loadWarehouse(request.sourceWarehouseId()))
                .destinationOrganization(loadOrganization(request.destinationOrganizationId()))
                .destinationWarehouse(loadWarehouse(request.destinationWarehouseId()))
                .proposedByUser(loadUser(userId))
                .status("PROPOSED")
                .recommendationReason(request.recommendationReason())
                .build();

        for (SwitchingStockRequest.ItemRequest itemRequest : request.items()) {
            Item item = itemRepository.findById(itemRequest.itemId())
                    .orElseThrow(() -> new RuntimeException("Barang tidak ditemukan: " + itemRequest.itemId()));
            switchingStock.getItems().add(SwitchingStockItem.builder()
                    .switchingStock(switchingStock)
                    .item(item)
                    .qtyRequested(itemRequest.qty())
                    .notes(itemRequest.notes())
                    .build());
        }

        SwitchingStock saved = switchingStockRepository.save(switchingStock);
        return SwitchingStockResponse.from(switchingStockRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Transactional
    public SwitchingStockResponse approveSwitching(Long id, Long approverId) {
        SwitchingStock switchingStock = loadSwitching(id);
        switchingStock.setStatus("APPROVED");
        switchingStock.setApprovedByUser(loadUser(approverId));
        switchingStock.getItems().forEach(item -> item.setQtyApproved(item.getQtyRequested()));
        SwitchingStock saved = switchingStockRepository.save(switchingStock);
        return SwitchingStockResponse.from(switchingStockRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    @Transactional
    public SwitchingStockResponse rejectSwitching(Long id, String reason, Long approverId) {
        SwitchingStock switchingStock = loadSwitching(id);
        switchingStock.setStatus("REJECTED");
        switchingStock.setApprovedByUser(loadUser(approverId));
        switchingStock.setRejectionReason(reason);
        SwitchingStock saved = switchingStockRepository.save(switchingStock);
        return SwitchingStockResponse.from(switchingStockRepository.findByIdWithDetails(saved.getId()).orElse(saved));
    }

    private SwitchingStock loadSwitching(Long id) {
        return switchingStockRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Pengajuan switching tidak ditemukan: " + id));
    }

    private Organization loadOrganization(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisasi tidak ditemukan: " + id));
    }

    private Warehouse loadWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gudang tidak ditemukan: " + id));
    }

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan: " + id));
    }

    private Order loadOrder(Long id) {
        if (id == null) {
            return null;
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + id));
    }
}
