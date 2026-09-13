package com.bankjatim.jims.dto;

import com.bankjatim.jims.security.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserRequest {

    @NotBlank(message = "Nama pengguna tidak boleh kosong")
    private String name;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    @Size(max = 50, message = "NIP maksimal 50 karakter")
    private String nip;

    private String password;

    @NotNull(message = "Role tidak boleh kosong")
    private UserRole role;

    private Long organizationId;
    private Long warehouseId;
    private BigDecimal approvalLimit = BigDecimal.ZERO;
    private String phone;
    private Boolean isActive = true;
}
