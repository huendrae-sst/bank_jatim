package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Courier extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "sla_days", nullable = false)
    @Builder.Default
    private Integer slaDays = 2;

    @Column(length = 50)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
