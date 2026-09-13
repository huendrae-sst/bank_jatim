package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vendor extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "sla_days", nullable = false)
    @Builder.Default
    private Integer slaDays = 7;

    @Column(name = "payment_terms", length = 100)
    @Builder.Default
    private String paymentTerms = "TOP 30 Hari";

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = new BigDecimal("5.00");

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
