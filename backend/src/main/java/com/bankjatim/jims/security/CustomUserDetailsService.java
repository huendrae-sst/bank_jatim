package com.bankjatim.jims.security;

import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithOrgAndWarehouse(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan dengan email: " + email));

        return UserPrincipal.create(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getNip(),
                user.getPassword(),
                user.getRole().getCode(),
                user.getOrganization() != null ? user.getOrganization().getId() : null,
                user.getWarehouse() != null ? user.getWarehouse().getId() : null,
                user.getRegion() != null ? user.getRegion().getId() : null,
                user.getRegion() != null ? user.getRegion().getName() : null,
                user.getApprovalLimit(),
                user.getIsActive() != null ? user.getIsActive() : true
        );
    }
}
