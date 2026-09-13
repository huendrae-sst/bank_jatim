package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "target_role", length = 50)
    private String targetRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_organization_id")
    private Organization targetOrganization;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String type = "INFORMATION"; // ACTION_REQUIRED, ALERT, INFORMATION

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String priority = "INFO"; // INFO, WARNING, HIGH, CRITICAL

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "reference_transaction_type", length = 50)
    private String referenceTransactionType;

    @Column(name = "reference_transaction_id")
    private Long referenceTransactionId;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
