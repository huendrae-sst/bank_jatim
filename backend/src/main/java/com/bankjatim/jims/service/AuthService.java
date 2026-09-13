package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.LoginRequest;
import com.bankjatim.jims.dto.LoginResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.JwtTokenProvider;
import com.bankjatim.jims.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByEmailWithOrgAndWarehouse(principal.getEmail())
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

        return LoginResponse.builder()
                .token(jwt)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .nip(user.getNip())
                .role(user.getRole())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .organizationCode(user.getOrganization() != null ? user.getOrganization().getCode() : null)
                .warehouseId(user.getWarehouse() != null ? user.getWarehouse().getId() : null)
                .approvalLimit(user.getApprovalLimit())
                .build();
    }
}
