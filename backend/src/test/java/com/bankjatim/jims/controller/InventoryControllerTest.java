package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.common.PageResponse;
import com.bankjatim.jims.domain.Category;
import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.Organization;
import com.bankjatim.jims.domain.StockBalance;
import com.bankjatim.jims.domain.Warehouse;
import com.bankjatim.jims.dto.StockBalanceResponse;
import com.bankjatim.jims.service.InventoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void stockBalancesResponseDoesNotExposeWarehouseOrganizationAssociation() throws Exception {
        InventoryService inventoryService = new StubInventoryService(stockBalance());
        InventoryController controller = new InventoryController(inventoryService, null);

        ResponseEntity<ApiResponse<PageResponse<StockBalanceResponse>>> response =
                controller.getStockBalances(null, PageRequest.of(0, 20));

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()));
        JsonNode firstBalance = root.path("data").path("content").get(0);
        assertThat(firstBalance.path("item").path("sku").asText()).isEqualTo("SKU-001");
        assertThat(firstBalance.path("item").path("category").path("name").asText()).isEqualTo("Kartu");
        assertThat(firstBalance.path("warehouse").path("code").asText()).isEqualTo("WH-001");
        assertThat(firstBalance.path("warehouse").has("organization")).isFalse();
    }

    private static StockBalance stockBalance() {
        Category category = Category.builder()
                .code("CARD")
                .name("Kartu")
                .build();
        category.setId(3L);

        Item item = Item.builder()
                .category(category)
                .sku("SKU-001")
                .name("Blanko Kartu")
                .uom("PCS")
                .estimatedUnitPrice(BigDecimal.valueOf(12500))
                .build();
        item.setId(2L);

        Organization organization = Organization.builder()
                .code("ORG-001")
                .name("Kantor Pusat")
                .type("HEAD_OFFICE")
                .build();
        organization.setId(8L);

        Warehouse warehouse = Warehouse.builder()
                .organization(organization)
                .code("WH-001")
                .name("Gudang Sentral")
                .type("CENTRAL_LOGISTICS")
                .build();
        warehouse.setId(1L);

        StockBalance balance = StockBalance.builder()
                .warehouse(warehouse)
                .item(item)
                .onHand(100)
                .reserved(10)
                .allocated(5)
                .inTransit(2)
                .hold(1)
                .damaged(0)
                .build();
        balance.setId(4L);
        return balance;
    }

    private static class StubInventoryService extends InventoryService {
        private final StockBalance stockBalance;

        StubInventoryService(StockBalance stockBalance) {
            super(null, null, null, null);
            this.stockBalance = stockBalance;
        }

        @Override
        public Page<StockBalance> getStockBalances(Long warehouseId, Pageable pageable) {
            return new PageImpl<>(List.of(stockBalance), pageable, 1);
        }
    }
}
