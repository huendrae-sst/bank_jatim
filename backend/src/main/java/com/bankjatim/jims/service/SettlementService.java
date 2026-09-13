package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.SettlementResponse;
import com.bankjatim.jims.repository.GeneralLedgerEntryRepository;
import com.bankjatim.jims.repository.OrderRepository;
import com.bankjatim.jims.repository.OrganizationRepository;
import com.bankjatim.jims.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final OrganizationRepository organizationRepository;
    private final GeneralLedgerEntryRepository glEntryRepository;

    @Transactional(readOnly = true)
    public List<SettlementResponse> getAllSettlements() {
        return settlementRepository.findAll().stream()
                .map(SettlementResponse::from)
                .toList();
    }

    @Transactional
    public SettlementResponse createSettlementFromOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan: " + orderId));

        Organization headOffice = organizationRepository.findByCode("KP-001")
                .orElseGet(() -> organizationRepository.findAll().get(0));

        String settlementNumber = "SETTLE-" + System.currentTimeMillis();

        Settlement settlement = Settlement.builder()
                .settlementNumber(settlementNumber)
                .order(order)
                .debitOrganization(order.getRequestingOrganization())
                .creditOrganization(headOffice)
                .debitCostCenter(order.getRequestingOrganization().getCostCenterCode() != null
                        ? order.getRequestingOrganization().getCostCenterCode() : "CC-BRANCH")
                .creditCostCenter("CC-LOG-01")
                .itemAmount(order.getTotalEstimatedValue())
                .shippingAmount(order.getIsPickupKp() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal("25000.00"))
                .totalAmount(order.getTotalEstimatedValue().add(order.getIsPickupKp() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal("25000.00")))
                .status("DRAFT")
                .createdByUser(user)
                .build();

        return SettlementResponse.from(settlementRepository.save(settlement));
    }

    @Transactional
    public SettlementResponse approveAndPost(Long settlementId, User approver) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement tidak ditemukan: " + settlementId));

        settlement.setStatus("POSTED");
        settlement.setApprovedByUser(approver);
        settlement.setPostedAt(LocalDateTime.now());

        // 1. Debit Entry (Beban Cabang: 51200 Beban Logistik & Cetakan)
        GeneralLedgerEntry debitEntry = GeneralLedgerEntry.builder()
                .transactionDate(LocalDate.now())
                .referenceNumber(settlement.getSettlementNumber())
                .accountCode("51200")
                .costCenterCode(settlement.getDebitCostCenter())
                .organization(settlement.getDebitOrganization())
                .debitAmount(settlement.getTotalAmount())
                .creditAmount(java.math.BigDecimal.ZERO)
                .description("Pembebanan Logistik Cabang: " + settlement.getDebitOrganization().getName())
                .createdByUser(approver)
                .build();
        glEntryRepository.save(debitEntry);

        // 2. Credit Entry (Kredit Persediaan Pusat: 11400)
        GeneralLedgerEntry creditEntry = GeneralLedgerEntry.builder()
                .transactionDate(LocalDate.now())
                .referenceNumber(settlement.getSettlementNumber())
                .accountCode("11400")
                .costCenterCode(settlement.getCreditCostCenter())
                .organization(settlement.getCreditOrganization())
                .debitAmount(java.math.BigDecimal.ZERO)
                .creditAmount(settlement.getTotalAmount())
                .description("Pengurangan Persediaan Logistik Pusat JIMS")
                .createdByUser(approver)
                .build();
        glEntryRepository.save(creditEntry);

        return SettlementResponse.from(settlementRepository.save(settlement));
    }
}
