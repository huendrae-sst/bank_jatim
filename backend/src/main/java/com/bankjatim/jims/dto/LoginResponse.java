package com.bankjatim.jims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String nip;
    private String role;
    private Long organizationId;
    private String organizationName;
    private String organizationCode;
    private Long warehouseId;
    private BigDecimal approvalLimit;
}
