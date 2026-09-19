package com.bankjatim.jims.dto;

import com.bankjatim.jims.domain.EmbossFile;

import java.time.LocalDateTime;

public record EmbossFileResponse(
        Long id,
        String fileId,
        String filename,
        String filePath,
        Integer totalRecords,
        Integer validRecords,
        Integer rejectedRecords,
        String status,
        String errorMessage,
        OrderResponse.UserSummary uploadedByUser,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EmbossFileResponse from(EmbossFile file) {
        return new EmbossFileResponse(
                file.getId(),
                file.getFileId() != null ? file.getFileId() : ("#EB-" + file.getId()),
                file.getFilename(),
                file.getFilePath(),
                file.getTotalRecords(),
                file.getValidRecords(),
                file.getRejectedRecords(),
                file.getStatus(),
                file.getErrorMessage(),
                OrderResponse.UserSummary.from(file.getUploadedByUser()),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }
}
