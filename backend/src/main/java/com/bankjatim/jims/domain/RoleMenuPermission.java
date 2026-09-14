package com.bankjatim.jims.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_menu_permissions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuPermission {

    @EmbeddedId
    private RoleMenuPermissionId id;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @MapsId("menuId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    public static RoleMenuPermission of(Role role, Menu menu) {
        return new RoleMenuPermission(
                new RoleMenuPermissionId(role.getId(), menu.getId()),
                role,
                menu
        );
    }
}
