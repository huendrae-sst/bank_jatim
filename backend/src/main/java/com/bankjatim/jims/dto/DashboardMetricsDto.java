package com.bankjatim.jims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDto {

    private long totalItems;
    private long lowStockCount;
    private long pendingApprovals;
    private long activeShipments;
    private BigDecimal totalInventoryValuation;
    private BigDecimal totalBudgetRemaining;
    private double stockoutIncidentRate;
    private double fulfillmentSlaRate;

    // Charts data
    private List<String> months;
    private List<BigDecimal> monthlyProcurementCost;
    private List<BigDecimal> monthlyCostSaving;
    private Map<String, Integer> stockStatusDistribution;
    private List<Map<String, Object>> topOrderedItems;
}
