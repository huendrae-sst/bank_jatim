package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Courier;
import com.bankjatim.jims.domain.ExpeditionMapping;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.dto.ExpeditionMappingRequest;
import com.bankjatim.jims.dto.ExpeditionMappingResponse;
import com.bankjatim.jims.repository.CourierRepository;
import com.bankjatim.jims.repository.ExpeditionMappingRepository;
import com.bankjatim.jims.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpeditionMappingService {

    private final ExpeditionMappingRepository expeditionMappingRepository;
    private final OrganizationRepository organizationRepository;
    private final CourierRepository courierRepository;

    @Transactional(readOnly = true)
    public List<ExpeditionMappingResponse> getMappings() {
        return expeditionMappingRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(ExpeditionMappingResponse::from)
                .toList();
    }

    @Transactional
    public ExpeditionMappingResponse createMapping(ExpeditionMappingRequest request) {
        ExpeditionMapping mapping = ExpeditionMapping.builder()
                .destinationOrganization(loadOrganization(request.destinationOrganizationId()))
                .courier(loadCourier(request.courierId()))
                .serviceType(normalizeServiceType(request.serviceType()))
                .estimatedLeadDays(request.estimatedLeadDays())
                .build();
        return ExpeditionMappingResponse.from(expeditionMappingRepository.save(mapping));
    }

    @Transactional
    public ExpeditionMappingResponse updateMapping(Long id, ExpeditionMappingRequest request) {
        ExpeditionMapping mapping = expeditionMappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pemetaan ekspedisi tidak ditemukan: " + id));
        mapping.setDestinationOrganization(loadOrganization(request.destinationOrganizationId()));
        mapping.setCourier(loadCourier(request.courierId()));
        mapping.setServiceType(normalizeServiceType(request.serviceType()));
        mapping.setEstimatedLeadDays(request.estimatedLeadDays());
        return ExpeditionMappingResponse.from(expeditionMappingRepository.save(mapping));
    }

    @Transactional
    public void deleteMapping(Long id) {
        if (!expeditionMappingRepository.existsById(id)) {
            throw new RuntimeException("Pemetaan ekspedisi tidak ditemukan: " + id);
        }
        expeditionMappingRepository.deleteById(id);
    }

    private Organization loadOrganization(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisasi tidak ditemukan: " + id));
    }

    private Courier loadCourier(Long id) {
        return courierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kurir tidak ditemukan: " + id));
    }

    private String normalizeServiceType(String serviceType) {
        return serviceType != null && !serviceType.isBlank() ? serviceType : "REGULER";
    }
}
