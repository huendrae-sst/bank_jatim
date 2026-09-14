package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.Courier;
import com.bankjatim.jims.domain.ExpeditionMapping;
import com.bankjatim.jims.domain.Menu;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.Role;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.ExpeditionMappingResponse;
import com.bankjatim.jims.dto.MenuResponse;
import com.bankjatim.jims.dto.UserResponse;
import com.bankjatim.jims.repository.BudgetRepository;
import com.bankjatim.jims.repository.CategoryRepository;
import com.bankjatim.jims.repository.CourierRepository;
import com.bankjatim.jims.repository.ItemRepository;
import com.bankjatim.jims.repository.MenuRepository;
import com.bankjatim.jims.repository.OrganizationRepository;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.repository.VendorRepository;
import com.bankjatim.jims.repository.WarehouseRepository;
import com.bankjatim.jims.service.ExpeditionMappingService;
import com.bankjatim.jims.service.MenuService;
import com.bankjatim.jims.service.RoleMenuService;
import com.bankjatim.jims.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterDataControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void usersEndpointReturnsDatabaseUsersWithoutPassword() throws Exception {
        UserRepository userRepository = stubUserRepository(user());
        MasterDataController controller = new MasterDataController(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new UserService(userRepository, null, null, null, null),
                null,
                null
        );

        ResponseEntity<ApiResponse<List<UserResponse>>> response = controller.getUsers();

        JsonNode firstUser = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()))
                .path("data")
                .get(0);
        assertThat(firstUser.path("name").asText()).isEqualTo("Ayu Lestari");
        assertThat(firstUser.path("email").asText()).isEqualTo("ayu.lestari@bankjatim.co.id");
        assertThat(firstUser.path("nip").asText()).isEqualTo("199001012020122001");
        assertThat(firstUser.path("role").asText()).isEqualTo("USER_ADMIN");
        assertThat(firstUser.path("organization").path("name").asText()).isEqualTo("Kantor Pusat");
        assertThat(firstUser.has("password")).isFalse();
    }

    @Test
    void expeditionMappingsEndpointReturnsDtoWithoutLazyCourierOrganizationProxy() throws Exception {
        ExpeditionMapping mapping = expeditionMapping();
        MasterDataController controller = new MasterDataController(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new StubExpeditionMappingService(mapping),
                null
        );

        ResponseEntity<ApiResponse<List<ExpeditionMappingResponse>>> response = controller.getExpeditionMappings();

        JsonNode firstMapping = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()))
                .path("data")
                .get(0);
        assertThat(firstMapping.path("destinationOrganization").path("name").asText()).isEqualTo("KC Malang");
        assertThat(firstMapping.path("courier").path("name").asText()).isEqualTo("Armada Internal");
        assertThat(firstMapping.path("serviceType").asText()).isEqualTo("REGULER");
        assertThat(firstMapping.path("estimatedLeadDays").asInt()).isEqualTo(2);
        assertThat(firstMapping.path("destinationOrganization").has("warehouses")).isFalse();
        assertThat(firstMapping.path("courier").has("shipments")).isFalse();
    }

    @Test
    void menusEndpointReturnsMenusWithRoles() throws Exception {
        MenuRepository menuRepository = stubMenuRepository(menu());
        MasterDataController controller = new MasterDataController(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new MenuService(menuRepository, new StubRoleMenuService())
        );

        ResponseEntity<ApiResponse<List<MenuResponse>>> response = controller.getMenus();

        JsonNode firstMenu = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()))
                .path("data")
                .get(0);
        assertThat(firstMenu.path("code").asText()).isEqualTo("MST_MENUS");
        assertThat(firstMenu.path("title").asText()).isEqualTo("Manajemen Menu");
        assertThat(firstMenu.path("module").asText()).isEqualTo("Master Data");
        assertThat(firstMenu.path("path").asText()).isEqualTo("/master/menus");
        assertThat(firstMenu.path("roles").get(0).asText()).isEqualTo("SUPER_ADMIN");
        assertThat(firstMenu.path("roles").get(1).asText()).isEqualTo("USER_ADMIN");
    }

    private static Menu menu() {
        Menu menu = Menu.builder()
                .code("MST_MENUS")
                .title("Manajemen Menu")
                .module("Master Data")
                .path("/master/menus")
                .icon("bi-menu-button-wide")
                .sortOrder(78)
                .status("AKTIF")
                .description("Pengaturan menu navigasi dan hak akses peran")
                .build();
        menu.setId(100L);
        return menu;
    }

    private static MenuRepository stubMenuRepository(Menu menu) {
        return (MenuRepository) Proxy.newProxyInstance(
                MenuRepository.class.getClassLoader(),
                new Class<?>[]{MenuRepository.class},
                (proxy, method, args) -> {
                    if ("findAllByOrderBySortOrderAsc".equals(method.getName())) {
                        return List.of(menu);
                    }
                    if ("findAll".equals(method.getName())) {
                        return List.of(menu);
                    }
                    if ("save".equals(method.getName())) {
                        return args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static User user() {
        Organization organization = Organization.builder()
                .code("KP-001")
                .name("Kantor Pusat")
                .type("HEAD_OFFICE")
                .build();
        organization.setId(1L);

        User user = User.builder()
                .organization(organization)
                .name("Ayu Lestari")
                .email("ayu.lestari@bankjatim.co.id")
                .nip("199001012020122001")
                .password("secret")
                .role(Role.builder().code("USER_ADMIN").name("User Admin").systemRole(true).build())
                .build();
        user.setId(10L);
        return user;
    }

    private static UserRepository stubUserRepository(User user) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findAllWithOrgAndWarehouse".equals(method.getName())) {
                        return List.of(user);
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static ExpeditionMapping expeditionMapping() {
        Organization organization = Organization.builder()
                .code("KC-MLG")
                .name("KC Malang")
                .type("BRANCH")
                .build();
        organization.setId(4L);

        Courier courier = Courier.builder()
                .code("CR-001")
                .name("Armada Internal")
                .slaDays(1)
                .isActive(true)
                .build();
        courier.setId(6L);

        ExpeditionMapping mapping = ExpeditionMapping.builder()
                .destinationOrganization(organization)
                .courier(courier)
                .serviceType("REGULER")
                .estimatedLeadDays(2)
                .build();
        mapping.setId(9L);
        return mapping;
    }

    private static class StubExpeditionMappingService extends ExpeditionMappingService {
        private final ExpeditionMapping mapping;

        StubExpeditionMappingService(ExpeditionMapping mapping) {
            super(null, null, null);
            this.mapping = mapping;
        }

        @Override
        public List<ExpeditionMappingResponse> getMappings() {
            return List.of(ExpeditionMappingResponse.from(mapping));
        }
    }

    private static class StubRoleMenuService extends RoleMenuService {
        StubRoleMenuService() {
            super(null, null, null);
        }

        @Override
        public java.util.Map<Long, List<String>> getRoleCodesByMenuIds(java.util.Collection<Long> menuIds) {
            return java.util.Map.of(100L, List.of("SUPER_ADMIN", "USER_ADMIN"));
        }
    }
}
