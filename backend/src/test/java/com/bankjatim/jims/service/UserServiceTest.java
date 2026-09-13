package com.bankjatim.jims.service;

import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.domain.Warehouse;
import com.bankjatim.jims.dto.UserRequest;
import com.bankjatim.jims.dto.UserResponse;
import com.bankjatim.jims.repository.OrganizationRepository;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.repository.WarehouseRepository;
import com.bankjatim.jims.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

    @Test
    void createUserPersistsHashedPasswordAndResolvedPlacement() {
        RepositoryState state = new RepositoryState();
        UserService service = new UserService(
                userRepository(state),
                organizationRepository(state.organization),
                warehouseRepository(state.warehouse),
                passwordEncoder()
        );

        UserResponse response = service.createUser(createRequest());

        User saved = state.users.get(response.id());
        assertThat(response.email()).isEqualTo("operator@bankjatim.co.id");
        assertThat(response.organization().name()).isEqualTo("Kantor Pusat");
        assertThat(response.warehouse().name()).isEqualTo("Gudang Sentral");
        assertThat(saved.getPassword()).isEqualTo("hashed:secret123");
    }

    @Test
    void updateUserKeepsExistingPasswordWhenRequestPasswordBlank() {
        RepositoryState state = new RepositoryState();
        User existing = existingUser(state.organization);
        state.users.put(existing.getId(), existing);
        UserService service = new UserService(
                userRepository(state),
                organizationRepository(state.organization),
                warehouseRepository(state.warehouse),
                passwordEncoder()
        );
        UserRequest request = createRequest();
        request.setName("Operator Updated");
        request.setEmail("operator.updated@bankjatim.co.id");
        request.setPassword(" ");

        UserResponse response = service.updateUser(existing.getId(), request);

        assertThat(response.name()).isEqualTo("Operator Updated");
        assertThat(state.users.get(existing.getId()).getPassword()).isEqualTo("existing-hash");
    }

    @Test
    void deleteUserRemovesExistingUser() {
        RepositoryState state = new RepositoryState();
        User existing = existingUser(state.organization);
        state.users.put(existing.getId(), existing);
        UserService service = new UserService(
                userRepository(state),
                organizationRepository(state.organization),
                warehouseRepository(state.warehouse),
                passwordEncoder()
        );

        service.deleteUser(existing.getId());

        assertThat(state.users).doesNotContainKey(existing.getId());
    }

    private static UserRequest createRequest() {
        UserRequest request = new UserRequest();
        request.setName("Operator");
        request.setEmail("operator@bankjatim.co.id");
        request.setNip("199001012020122001");
        request.setPassword("secret123");
        request.setRole(UserRole.USER_ADMIN);
        request.setOrganizationId(1L);
        request.setWarehouseId(2L);
        request.setApprovalLimit(BigDecimal.valueOf(1_000_000));
        request.setPhone("031-123456");
        request.setIsActive(true);
        return request;
    }

    private static User existingUser(Organization organization) {
        User user = User.builder()
                .organization(organization)
                .name("Operator")
                .email("operator@bankjatim.co.id")
                .nip("199001012020122001")
                .password("existing-hash")
                .role(UserRole.USER_ADMIN)
                .build();
        user.setId(10L);
        return user;
    }

    private static UserRepository userRepository(RepositoryState state) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existsByEmail" -> state.users.values().stream()
                            .anyMatch(user -> user.getEmail().equals(args[0]));
                    case "existsByNip" -> state.users.values().stream()
                            .anyMatch(user -> args[0] != null && args[0].equals(user.getNip()));
                    case "findByEmail" -> state.users.values().stream()
                            .filter(user -> user.getEmail().equals(args[0]))
                            .findFirst();
                    case "findByNip" -> state.users.values().stream()
                            .filter(user -> args[0] != null && args[0].equals(user.getNip()))
                            .findFirst();
                    case "findById" -> Optional.ofNullable(state.users.get(args[0]));
                    case "existsById" -> state.users.containsKey(args[0]);
                    case "save" -> {
                        User user = (User) args[0];
                        if (user.getId() == null) {
                            user.setId(state.nextUserId++);
                        }
                        state.users.put(user.getId(), user);
                        yield user;
                    }
                    case "deleteById" -> {
                        state.users.remove(args[0]);
                        yield null;
                    }
                    case "findAllWithOrgAndWarehouse" -> state.users.values().stream().toList();
                    case "toString" -> "UserRepositoryStub";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static OrganizationRepository organizationRepository(Organization organization) {
        return (OrganizationRepository) Proxy.newProxyInstance(
                OrganizationRepository.class.getClassLoader(),
                new Class<?>[]{OrganizationRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.of(organization);
                    case "toString" -> "OrganizationRepositoryStub";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static WarehouseRepository warehouseRepository(Warehouse warehouse) {
        return (WarehouseRepository) Proxy.newProxyInstance(
                WarehouseRepository.class.getClassLoader(),
                new Class<?>[]{WarehouseRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.of(warehouse);
                    case "toString" -> "WarehouseRepositoryStub";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "hashed:" + rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals(encode(rawPassword));
            }
        };
    }

    private static class RepositoryState {
        private long nextUserId = 1L;
        private final Map<Long, User> users = new HashMap<>();
        private final Organization organization;
        private final Warehouse warehouse;

        private RepositoryState() {
            organization = Organization.builder()
                    .code("KP-001")
                    .name("Kantor Pusat")
                    .type("HEAD_OFFICE")
                    .build();
            organization.setId(1L);

            warehouse = Warehouse.builder()
                    .organization(organization)
                    .code("WH-001")
                    .name("Gudang Sentral")
                    .type("CENTRAL_LOGISTICS")
                    .build();
            warehouse.setId(2L);
        }
    }
}
