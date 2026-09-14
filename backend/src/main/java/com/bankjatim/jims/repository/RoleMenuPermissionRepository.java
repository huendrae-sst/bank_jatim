package com.bankjatim.jims.repository;

import com.bankjatim.jims.domain.RoleMenuPermission;
import com.bankjatim.jims.domain.RoleMenuPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoleMenuPermissionRepository extends JpaRepository<RoleMenuPermission, RoleMenuPermissionId> {
    List<RoleMenuPermission> findAllByRole_CodeOrderByMenu_SortOrderAsc(String roleCode);
    List<RoleMenuPermission> findAllByOrderByRole_CodeAscMenu_SortOrderAsc();
    List<RoleMenuPermission> findAllByMenu_IdIn(Collection<Long> menuIds);
    void deleteAllByRole_Id(Long roleId);
    void deleteAllByMenu_Id(Long menuId);
}
