package com.bankjatim.jims.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RegionRequest {

    @NotBlank(message = "Kode wilayah tidak boleh kosong")
    private String code;

    @NotBlank(message = "Nama wilayah tidak boleh kosong")
    private String name;

    private String description;
    private Boolean isActive = true;
    private List<Long> organizationIds;
}
