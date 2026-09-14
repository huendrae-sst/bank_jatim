package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.AuditLog;
import com.bankjatim.jims.domain.Notification;
import com.bankjatim.jims.domain.Role;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.AuditLogResponse;
import com.bankjatim.jims.dto.NotificationResponse;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.AuditLogService;
import com.bankjatim.jims.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditNotificationControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void auditTrailReturnsDtoWithoutUserAssociationProxy() throws Exception {
        AuditLog log = auditLog();
        AuditController controller = new AuditController(new StubAuditLogService(log));

        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response = controller.getAuditLogs(null);

        JsonNode firstLog = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()))
                .path("data")
                .get(0);
        assertThat(firstLog.path("actor").asText()).isEqualTo("Ayu Lestari");
        assertThat(firstLog.path("role").asText()).isEqualTo("USER_ADMIN");
        assertThat(firstLog.path("module").asText()).isEqualTo("ORDERS");
        assertThat(firstLog.path("ref").asText()).isEqualTo("ORDERS-99");
        assertThat(firstLog.has("user")).isFalse();
    }

    @Test
    void notificationsReturnsCurrentUserNotificationsAndCanMarkAllRead() throws Exception {
        Notification notification = notification();
        StubNotificationService service = new StubNotificationService(notification);
        NotificationController controller = new NotificationController(service);
        UserPrincipal principal = UserPrincipal.create(
                7L,
                "Ayu Lestari",
                "ayu@example.test",
                "199001",
                "secret",
                "USER_ADMIN",
                1L,
                null,
                null,
                true
        );

        ResponseEntity<ApiResponse<List<NotificationResponse>>> response = controller.getNotifications(principal);
        controller.markAllRead(principal);

        JsonNode firstNotification = objectMapper.readTree(objectMapper.writeValueAsString(response.getBody()))
                .path("data")
                .get(0);
        assertThat(firstNotification.path("title").asText()).isEqualTo("Approval diperlukan");
        assertThat(firstNotification.path("link").asText()).isEqualTo("/orders/approvals");
        assertThat(firstNotification.path("isRead").asBoolean()).isFalse();
        assertThat(service.markedUserId).isEqualTo(7L);
    }

    private static AuditLog auditLog() {
        User user = User.builder()
                .name("Ayu Lestari")
                .email("ayu@example.test")
                .password("secret")
                .nip("199001")
                .role(Role.builder().code("USER_ADMIN").name("User Admin").systemRole(true).build())
                .build();
        user.setId(7L);

        AuditLog log = AuditLog.builder()
                .user(user)
                .action("APPROVE_ORDER")
                .auditableType("ORDERS")
                .auditableId(99L)
                .ipAddress("10.14.1.10")
                .newValues("{\"status\":\"APPROVED\"}")
                .build();
        log.setId(15L);
        log.setCreatedAt(LocalDateTime.of(2026, 9, 13, 8, 30));
        return log;
    }

    private static Notification notification() {
        Notification notification = Notification.builder()
                .title("Approval diperlukan")
                .message("Order baru menunggu persetujuan")
                .type("ACTION_REQUIRED")
                .priority("HIGH")
                .actionUrl("/orders/approvals")
                .isRead(false)
                .build();
        notification.setId(21L);
        notification.setCreatedAt(LocalDateTime.of(2026, 9, 13, 9, 15));
        return notification;
    }

    private static class StubAuditLogService extends AuditLogService {
        private final AuditLog log;

        StubAuditLogService(AuditLog log) {
            super(null);
            this.log = log;
        }

        @Override
        public List<AuditLogResponse> getAuditLogs(String module) {
            return List.of(AuditLogResponse.from(log));
        }
    }

    private static class StubNotificationService extends NotificationService {
        private final Notification notification;
        private Long markedUserId;

        StubNotificationService(Notification notification) {
            super(null);
            this.notification = notification;
        }

        @Override
        public List<NotificationResponse> getNotifications(UserPrincipal principal) {
            return List.of(NotificationResponse.from(notification));
        }

        @Override
        public void markAllRead(Long userId) {
            markedUserId = userId;
        }
    }
}
