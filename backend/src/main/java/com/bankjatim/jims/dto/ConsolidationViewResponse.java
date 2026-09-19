package com.bankjatim.jims.dto;

import java.util.List;

public record ConsolidationViewResponse(
        String branchCode,
        String branchName,
        Long orderId,
        String orderNumber,
        String cardType,
        Integer totalCards,
        String status,
        Long embossFileId,
        String embossFileName,
        List<String> customerNames
) {}
