package com.bankjatim.jims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(
        @NotBlank(message = "Nama role tidak boleh kosong")
        @Size(max = 150, message = "Nama role maksimal 150 karakter")
        String name,
        String description
) {
}
