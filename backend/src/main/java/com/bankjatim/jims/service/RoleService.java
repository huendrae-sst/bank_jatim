package com.bankjatim.jims.service;

import com.bankjatim.jims.common.BadRequestException;
import com.bankjatim.jims.common.ConflictException;
import com.bankjatim.jims.common.ResourceNotFoundException;
import com.bankjatim.jims.domain.Role;
import com.bankjatim.jims.dto.RoleCreateRequest;
import com.bankjatim.jims.dto.RoleResponse;
import com.bankjatim.jims.dto.RoleUpdateRequest;
import com.bankjatim.jims.repository.RoleRepository;
import com.bankjatim.jims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RoleService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAllByOrderByNameAscCodeAsc().stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        String code = normalizeCode(request.code());
        validateCode(code);
        if (roleRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Kode role '" + code + "' sudah digunakan");
        }
        Role role = Role.builder()
                .code(code)
                .name(requiredName(request.name()))
                .description(blankToNull(request.description()))
                .systemRole(false)
                .build();
        return RoleResponse.from(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(String code, RoleUpdateRequest request) {
        Role role = findRole(code);
        role.setName(requiredName(request.name()));
        role.setDescription(blankToNull(request.description()));
        return RoleResponse.from(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(String code) {
        Role role = findRole(code);
        if (Boolean.TRUE.equals(role.getSystemRole())) {
            throw new ConflictException("Role sistem tidak dapat dihapus");
        }
        long userCount = userRepository.countByRole_Code(role.getCode());
        if (userCount > 0) {
            throw new ConflictException("Role masih digunakan oleh " + userCount + " pengguna");
        }
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public Role findRole(String code) {
        String normalized = normalizeCode(code);
        return roleRepository.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Role tidak ditemukan: " + normalized));
    }

    public static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private static void validateCode(String code) {
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BadRequestException("Kode role hanya boleh berisi huruf, angka, dan garis bawah");
        }
    }

    private static String requiredName(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Nama role tidak boleh kosong");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
