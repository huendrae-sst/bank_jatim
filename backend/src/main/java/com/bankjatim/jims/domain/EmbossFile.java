package com.bankjatim.jims.domain;

import com.bankjatim.jims.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emboss_files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbossFile extends BaseEntity {

    @Column(name = "file_id", length = 50)
    private String fileId;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "total_records", nullable = false)
    @Builder.Default
    private Integer totalRecords = 0;

    @Column(name = "valid_records", nullable = false)
    @Builder.Default
    private Integer validRecords = 0;

    @Column(name = "rejected_records", nullable = false)
    @Builder.Default
    private Integer rejectedRecords = 0;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "UPLOADED";

    @Column(name = "fulfillment_status", nullable = false, length = 50)
    @Builder.Default
    private String fulfillmentStatus = "UNFULFILLED"; // UNFULFILLED, PARTIALLY_FULFILLED, FULLY_FULFILLED, RECEIVED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedByUser;

    @OneToMany(mappedBy = "embossFile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmbossRecord> records = new ArrayList<>();
}
