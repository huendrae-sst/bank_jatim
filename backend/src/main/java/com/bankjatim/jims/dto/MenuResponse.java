package com.bankjatim.jims.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private String id;
    private String code;
    private String title;
    private String module;
    private String parentCode;
    private String path;
    private String icon;

    @JsonProperty("order")
    private Integer order;

    private String status;
    private String description;
    private List<String> roles;
}
