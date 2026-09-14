package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.Role;
import com.bankjatim.jims.domain.SwitchingStock;
import com.bankjatim.jims.domain.SwitchingStockItem;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.domain.Warehouse;
import com.bankjatim.jims.dto.SwitchingStockResponse;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.SwitchingStockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwitchingStockControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void switchingListReturnsDtoWithoutLazyAssociations() throws Exception {
        SwitchingStockController controller = new SwitchingStockController(new StubSwitchingStockService(switchingStock()));

        ResponseEntity<ApiResponse<List<SwitchingStockResponse>>> response = controller.getSwitchingStocks(null);

        JsonNode first = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()))
                .path("data")
                .get(0);
        assertThat(first.path("sourceOrganization").path("name").asText()).isEqualTo("KC Sidoarjo");
        assertThat(first.path("destinationOrganization").path("name").asText()).isEqualTo("KC Malang");
        assertThat(first.path("items").get(0).path("item").path("name").asText()).isEqualTo("Buku Tabungan");
        assertThat(first.path("sourceWarehouse").has("organization")).isFalse();
        assertThat(first.path("proposedByUser").has("password")).isFalse();
    }

    @Test
    void approveSwitchingReturnsApprovedStatus() {
        SwitchingStockController controller = new SwitchingStockController(new StubSwitchingStockService(switchingStock()));
        UserPrincipal principal = UserPrincipal.create(
                11L,
                "Approver",
                "approver@example.test",
                "199002",
                "secret",
                "SWITCHING_APPROVER",
                1L,
                null,
                null,
                true
        );

        ResponseEntity<ApiResponse<SwitchingStockResponse>> response = controller.approveSwitching(1L, principal);

        assertThat(response.getBody().getData().status()).isEqualTo("APPROVED");
    }

    private static SwitchingStock switchingStock() {
        Organization source = Organization.builder().code("KC-SDA").name("KC Sidoarjo").type("BRANCH").build();
        source.setId(1L);
        Organization destination = Organization.builder().code("KC-MLG").name("KC Malang").type("BRANCH").build();
        destination.setId(2L);

        Warehouse sourceWarehouse = Warehouse.builder().code("WH-SDA").name("Gudang Sidoarjo").organization(source).type("BRANCH").build();
        sourceWarehouse.setId(3L);
        Warehouse destinationWarehouse = Warehouse.builder().code("WH-MLG").name("Gudang Malang").organization(destination).type("BRANCH").build();
        destinationWarehouse.setId(4L);

        User user = User.builder()
                .name("Ayu Lestari")
                .email("ayu@example.test")
                .password("secret")
                .role(Role.builder().code("INVENTORY_OFFICER").name("Inventory Officer").systemRole(true).build())
                .build();
        user.setId(5L);

        Item item = Item.builder().sku("BT-001").name("Buku Tabungan").uom("BUKU").build();
        item.setId(6L);

        SwitchingStock switching = SwitchingStock.builder()
                .sourceOrganization(source)
                .sourceWarehouse(sourceWarehouse)
                .destinationOrganization(destination)
                .destinationWarehouse(destinationWarehouse)
                .proposedByUser(user)
                .status("PROPOSED")
                .recommendationReason("Stok cabang tujuan defisit")
                .build();
        switching.setId(7L);

        SwitchingStockItem switchingItem = SwitchingStockItem.builder()
                .switchingStock(switching)
                .item(item)
                .qtyRequested(25)
                .qtyApproved(0)
                .build();
        switchingItem.setId(8L);
        switching.getItems().add(switchingItem);
        return switching;
    }

    private static class StubSwitchingStockService extends SwitchingStockService {
        private final SwitchingStock switchingStock;

        StubSwitchingStockService(SwitchingStock switchingStock) {
            super(null, null, null, null, null, null);
            this.switchingStock = switchingStock;
        }

        @Override
        public List<SwitchingStockResponse> getSwitchingStocks(String status) {
            return List.of(SwitchingStockResponse.from(switchingStock));
        }

        @Override
        public SwitchingStockResponse approveSwitching(Long id, Long approverId) {
            switchingStock.setStatus("APPROVED");
            return SwitchingStockResponse.from(switchingStock);
        }
    }
}
