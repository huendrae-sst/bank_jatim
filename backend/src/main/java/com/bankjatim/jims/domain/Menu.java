package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "menus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "module", nullable = false, length = 100)
    private String module;

    @Column(name = "parent_code", length = 50)
    private String parentCode;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "icon", nullable = false, length = 100)
    @Builder.Default
    private String icon = "bi-circle";

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 1;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "AKTIF";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "roles", columnDefinition = "TEXT", nullable = false)
    @Builder.Default
    private String roles = "SUPER_ADMIN";

    public List<String> getRoleList() {
        if (roles == null || roles.isBlank()) {
            return List.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void setRoleList(List<String> list) {
        if (list == null || list.isEmpty()) {
            this.roles = "SUPER_ADMIN";
        } else {
            this.roles = String.join(",", list);
        }
    }
}
