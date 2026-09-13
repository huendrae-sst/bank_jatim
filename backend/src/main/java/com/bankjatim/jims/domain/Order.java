package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 100)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_organization_id", nullable = false)
    private Organization requestingOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_warehouse_id")
    private Warehouse requestingWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String priority = "NORMAL"; // NORMAL, HIGH, URGENT

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "total_items", nullable = false)
    @Builder.Default
    private Integer totalItems = 0;

    @Column(name = "total_estimated_value", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalEstimatedValue = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "is_overbudget", nullable = false)
    @Builder.Default
    private Boolean isOverbudget = false;

    @Column(name = "delivery_method", nullable = false, length = 50)
    @Builder.Default
    private String deliveryMethod = "COURIER";

    @Column(name = "pickup_pic_nip", length = 50)
    private String pickupPicNip;

    @Column(name = "pickup_pic_name", length = 255)
    private String pickupPicName;

    @Column(name = "pickup_pic_position", length = 255)
    private String pickupPicPosition;

    @Column(name = "pickup_notes", columnDefinition = "TEXT")
    private String pickupNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Transient
    public Boolean getIsPickupKp() {
        return "PICKUP_KP".equalsIgnoreCase(deliveryMethod);
    }

    public void setIsPickupKp(Boolean isPickupKp) {
        this.deliveryMethod = Boolean.TRUE.equals(isPickupKp) ? "PICKUP_KP" : "COURIER";
    }
}
