package com.bankjatim.jims.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {

    @NotBlank(message = "Kode menu tidak boleh kosong")
    private String code;

    @NotBlank(message = "Judul menu tidak boleh kosong")
    private String title;

    @NotBlank(message = "Modul induk tidak boleh kosong")
    private String module;

    private String parentCode;

    @NotBlank(message = "Rute URL path tidak boleh kosong")
    private String path;

    @Builder.Default
    private String icon = "bi-circle";

    @JsonProperty("order")
    @Builder.Default
    private Integer order = 1;

    @Builder.Default
    private String status = "AKTIF";

    private String description;

    private List<String> roles;
}
