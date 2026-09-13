package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.Courier;
import com.bankjatim.jims.domain.ExpeditionMapping;
import com.bankjatim.jims.domain.Organization;

public record ExpeditionMappingResponse(
        Long id,
        OrganizationSummary destinationOrganization,
        CourierSummary courier,
        String serviceType,
        Integer estimatedLeadDays
) {
    public static ExpeditionMappingResponse from(ExpeditionMapping mapping) {
        return new ExpeditionMappingResponse(
                mapping.getId(),
                OrganizationSummary.from(mapping.getDestinationOrganization()),
                CourierSummary.from(mapping.getCourier()),
                mapping.getServiceType(),
                mapping.getEstimatedLeadDays()
        );
    }

    public record OrganizationSummary(Long id, String code, String name, String type) {
        static OrganizationSummary from(Organization organization) {
            if (organization == null) {
                return null;
            }
            return new OrganizationSummary(
                    organization.getId(),
                    organization.getCode(),
                    organization.getName(),
                    organization.getType()
            );
        }
    }

    public record CourierSummary(Long id, String code, String name, Integer slaDays, Boolean isActive) {
        static CourierSummary from(Courier courier) {
            if (courier == null) {
                return null;
            }
            return new CourierSummary(
                    courier.getId(),
                    courier.getCode(),
                    courier.getName(),
                    courier.getSlaDays(),
                    courier.getIsActive()
            );
        }
    }
}
