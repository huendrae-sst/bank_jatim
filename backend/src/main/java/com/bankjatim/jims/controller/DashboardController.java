package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.StockBalance;
import com.bankjatim.jims.dto.DashboardMetricsDto;
import com.bankjatim.jims.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints untuk data metrik KPI dan grafik Executive Support System (ESS)")
public class DashboardController {

    private final ItemRepository itemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final BudgetRepository budgetRepository;

    @GetMapping("/metrics")
    @Operation(summary = "Get Dashboard Metrics", description = "Menghasilkan ringkasan KPI, total valuasi persediaan, insiden stockout, dan data grafik ECharts.")
    public ResponseEntity<ApiResponse<DashboardMetricsDto>> getMetrics() {
        long totalItems = itemRepository.count();
        long lowStockCount = stockBalanceRepository.findLowStockItems().size();
        long outOfStockCount = stockBalanceRepository.countOutOfStockItems();
        long pendingApprovals = orderRepository.findPendingApprovals().size();
        long activeShipments = shipmentRepository.findByStatus("IN_TRANSIT").size();
        BigDecimal totalInventoryValuation = stockBalanceRepository.calculateTotalInventoryValuation();
        BigDecimal totalBudgetRemaining = budgetRepository.calculateTotalBudgetRemaining();

        Map<String, Integer> stockStatusDistribution = new HashMap<>();
        stockStatusDistribution.put("Aman", (int) Math.max(totalItems - lowStockCount, 0));
        stockStatusDistribution.put("Mendekati Reorder Point", (int) Math.max(lowStockCount - outOfStockCount, 0));
        stockStatusDistribution.put("Kritis / Out of Stock", (int) outOfStockCount);

        List<String> months = Collections.emptyList();
        List<BigDecimal> monthlyProcurement = Collections.emptyList();
        List<BigDecimal> monthlySaving = Collections.emptyList();

        List<Map<String, Object>> topItems = new ArrayList<>();
        for (StockBalance balance : stockBalanceRepository.findTopStockBalances(PageRequest.of(0, 5))) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", balance.getItem().getName());
            item.put("qty", balance.getOnHand());
            item.put("category", balance.getItem().getCategory().getName());
            topItems.add(item);
        }

        double stockoutIncidentRate = totalItems == 0 ? 0.0 : (outOfStockCount * 100.0) / totalItems;

        DashboardMetricsDto dto = DashboardMetricsDto.builder()
                .totalItems(totalItems)
                .lowStockCount(lowStockCount)
                .pendingApprovals(pendingApprovals)
                .activeShipments(activeShipments)
                .totalInventoryValuation(totalInventoryValuation)
                .totalBudgetRemaining(totalBudgetRemaining)
                .stockoutIncidentRate(stockoutIncidentRate)
                .fulfillmentSlaRate(0.0)
                .months(months)
                .monthlyProcurementCost(monthlyProcurement)
                .monthlyCostSaving(monthlySaving)
                .stockStatusDistribution(stockStatusDistribution)
                .topOrderedItems(topItems)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
