package com.bankjatim.jims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
        @NotBlank(message = "Kode role tidak boleh kosong")
        @Size(max = 50, message = "Kode role maksimal 50 karakter")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "Kode role hanya boleh berisi huruf, angka, dan garis bawah")
        String code,
        @NotBlank(message = "Nama role tidak boleh kosong")
        @Size(max = 150, message = "Nama role maksimal 150 karakter")
        String name,
        String description
) {
}
