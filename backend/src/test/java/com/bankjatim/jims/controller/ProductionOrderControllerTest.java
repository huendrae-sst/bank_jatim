package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.*;
import com.bankjatim.jims.dto.ProductionOrderResponse;
import com.bankjatim.jims.dto.EmbossRecordResponse;
import com.bankjatim.jims.service.ProductionOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionOrderControllerTest {

    @Test
    void testProductionOrderLifecycleEndpoints() {
        ProductionOrder po = createSamplePo();
        StubProductionOrderService stubService = new StubProductionOrderService(po);
        ProductionOrderController controller = new ProductionOrderController(stubService, null);

        // 1. List production orders
        ResponseEntity<ApiResponse<List<ProductionOrderResponse>>> listRes = controller.getAllProductionOrders();
        assertThat(listRes.getBody()).isNotNull();
        assertThat(listRes.getBody().getData()).hasSize(1);
        assertThat(listRes.getBody().getData().get(0).productionNumber()).isEqualTo("SPK-PROD-20260919-0001");

        // 2. Detail production order
        ResponseEntity<ApiResponse<ProductionOrderResponse>> detailRes = controller.getProductionOrder(1L);
        assertThat(detailRes.getBody()).isNotNull();
        assertThat(detailRes.getBody().getData().status()).isEqualTo("DRAFT");
        assertThat(detailRes.getBody().getData().warehouse().name()).isEqualTo("Gudang Sentral");

        // 3. Card traceability endpoint
        ResponseEntity<ApiResponse<List<EmbossRecordResponse>>> cardsRes = controller.getProductionCards(1L);
        assertThat(cardsRes.getBody()).isNotNull();
        assertThat(cardsRes.getBody().getData()).hasSize(1);
        assertThat(cardsRes.getBody().getData().get(0).customerName()).isEqualTo("Ahmad Dahlan");
        assertThat(cardsRes.getBody().getData().get(0).productionStatus()).isEqualTo("QUEUED");
    }

    private static ProductionOrder createSamplePo() {
        Warehouse wh = Warehouse.builder().code("WH-CEN-01").name("Gudang Sentral").type("CENTRAL_LOGISTICS").build();
        wh.setId(10L);

        User user = User.builder().name("Operator Produksi").email("operator@bankjatim.test").build();
        user.setId(5L);

        ProductionOrder po = ProductionOrder.builder()
                .productionNumber("SPK-PROD-20260919-0001")
                .warehouse(wh)
                .createdByUser(user)
                .productionDate(LocalDate.now())
                .status("DRAFT")
                .materialIssueStatus("PENDING")
                .totalQty(100)
                .totalProduced(0)
                .totalDamaged(0)
                .notes("Testing SPK")
                .build();
        po.setId(1L);
        return po;
    }

    private static class StubProductionOrderService extends ProductionOrderService {
        private final ProductionOrder po;

        StubProductionOrderService(ProductionOrder po) {
            super(null, null, null, null, null, null, null, null, null, null, null, null);
            this.po = po;
        }

        @Override
        public List<ProductionOrderResponse> getAllProductionOrders() {
            return List.of(ProductionOrderResponse.from(po));
        }

        @Override
        public ProductionOrderResponse getProductionOrder(Long id) {
            return ProductionOrderResponse.from(po);
        }

        @Override
        public List<EmbossRecordResponse> getProductionCards(Long id) {
            Item item = Item.builder().sku("ATM-EMB-GPN-001").name("Kartu GPN Emboss").build();
            item.setId(1L);
            EmbossRecord rec = EmbossRecord.builder()
                    .accountNumber("1112223334")
                    .customerName("Ahmad Dahlan")
                    .cardNumberMasked("6013 **** 8888")
                    .cardType("GPN")
                    .branchCode("KC-SBY")
                    .productionOrder(po)
                    .productionStatus("QUEUED")
                    .producedItem(item)
                    .status("VALID")
                    .build();
            rec.setId(99L);
            return List.of(EmbossRecordResponse.from(rec));
        }
    }
}
