package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.EmbossFile;
import com.bankjatim.jims.domain.EmbossRecord;
import com.bankjatim.jims.domain.Item;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.EmbossFileResponse;
import com.bankjatim.jims.dto.EmbossRecordResponse;
import com.bankjatim.jims.security.UserRole;
import com.bankjatim.jims.service.EmbossService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbossControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void embossDetailAndRecordsReturnSafeDtos() throws Exception {
        EmbossFile file = embossFile();
        EmbossController controller = new EmbossController(new StubEmbossService(file), null);

        ResponseEntity<ApiResponse<EmbossFileResponse>> fileResponse = controller.getEmbossFile(1L);
        ResponseEntity<ApiResponse<List<EmbossRecordResponse>>> recordsResponse = controller.getEmbossRecords(1L);

        JsonNode fileJson = objectMapper.readTree(objectMapper.writeValueAsString(fileResponse.getBody())).path("data");
        JsonNode firstRecord = objectMapper.readTree(objectMapper.writeValueAsString(recordsResponse.getBody()))
                .path("data")
                .get(0);

        assertThat(fileJson.path("filename").asText()).isEqualTo("emboss.csv");
        assertThat(firstRecord.path("customerName").asText()).isEqualTo("Ayu Lestari");
        assertThat(firstRecord.path("item").path("sku").asText()).isEqualTo("CARD-GPN");
        assertThat(firstRecord.has("embossFile")).isFalse();
    }

    private static EmbossFile embossFile() {
        User user = User.builder()
                .name("Uploader")
                .email("uploader@example.test")
                .password("secret")
                .role(UserRole.INVENTORY_OFFICER)
                .build();
        user.setId(7L);

        EmbossFile file = EmbossFile.builder()
                .filename("emboss.csv")
                .uploadedByUser(user)
                .totalRecords(1)
                .validRecords(1)
                .rejectedRecords(0)
                .status("VALIDATED")
                .build();
        file.setId(1L);

        Item item = Item.builder().sku("CARD-GPN").name("Kartu GPN").uom("PCS").build();
        item.setId(2L);
        EmbossRecord record = EmbossRecord.builder()
                .embossFile(file)
                .accountNumber("1234567890")
                .customerName("Ayu Lestari")
                .cardNumberMasked("6013 **** 0001")
                .cardType("GPN")
                .branchCode("KC-SBY")
                .item(item)
                .status("VALID")
                .build();
        record.setId(3L);
        file.getRecords().add(record);
        return file;
    }

    private static class StubEmbossService extends EmbossService {
        private final EmbossFile file;

        StubEmbossService(EmbossFile file) {
            super(null, null, null, null, null);
            this.file = file;
        }

        @Override
        public EmbossFileResponse getEmbossFile(Long id) {
            return EmbossFileResponse.from(file);
        }

        @Override
        public List<EmbossRecordResponse> getEmbossRecords(Long fileId, String status) {
            return file.getRecords().stream().map(EmbossRecordResponse::from).toList();
        }
    }
}
