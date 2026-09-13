package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expedition_mappings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpeditionMapping extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_organization_id", nullable = false, unique = true)
    private Organization destinationOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @Column(name = "service_type", length = 50)
    @Builder.Default
    private String serviceType = "REGULER";

    @Column(name = "estimated_lead_days", nullable = false)
    @Builder.Default
    private Integer estimatedLeadDays = 2;
}
