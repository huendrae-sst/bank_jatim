package com.bankjatim.jims.service;

import com.bankjatim.jims.common.BadRequestException;
import com.bankjatim.jims.common.ResourceNotFoundException;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.domain.Warehouse;
import com.bankjatim.jims.dto.UserRequest;
import com.bankjatim.jims.dto.UserResponse;
import com.bankjatim.jims.repository.OrganizationRepository;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAllWithOrgAndWarehouse().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password wajib diisi untuk pengguna baru");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email sudah digunakan");
        }
        if (hasText(request.getNip()) && userRepository.existsByNip(request.getNip())) {
            throw new BadRequestException("NIP sudah digunakan");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .nip(blankToNull(request.getNip()))
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .organization(resolveOrganization(request.getOrganizationId()))
                .warehouse(resolveWarehouse(request.getWarehouseId()))
                .approvalLimit(defaultMoney(request.getApprovalLimit()))
                .phone(blankToNull(request.getPhone()))
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna tidak ditemukan: " + id));

        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Email sudah digunakan");
                });

        if (hasText(request.getNip())) {
            userRepository.findByNip(request.getNip())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new BadRequestException("NIP sudah digunakan");
                    });
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setNip(blankToNull(request.getNip()));
        user.setRole(request.getRole());
        user.setOrganization(resolveOrganization(request.getOrganizationId()));
        user.setWarehouse(resolveWarehouse(request.getWarehouseId()));
        user.setApprovalLimit(defaultMoney(request.getApprovalLimit()));
        user.setPhone(blankToNull(request.getPhone()));
        user.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        if (hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pengguna tidak ditemukan: " + id);
        }
        userRepository.deleteById(id);
    }

    private Organization resolveOrganization(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisasi tidak ditemukan: " + organizationId));
    }

    private Warehouse resolveWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            return null;
        }
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Gudang tidak ditemukan: " + warehouseId));
    }

    private static BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
