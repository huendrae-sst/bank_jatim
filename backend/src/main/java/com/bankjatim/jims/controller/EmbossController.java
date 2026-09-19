package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.EmbossFileResponse;
import com.bankjatim.jims.dto.EmbossRecordResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.EmbossService;
import com.opencsv.CSVReader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/emboss")
@RequiredArgsConstructor
@Tag(name = "Card Personalization & Emboss", description = "Endpoints untuk Integrasi Berkas Emboss ATM/Debit dari Core Banking")
public class EmbossController {

    private final EmbossService embossService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Daftar Berkas Emboss Terunggah")
    public ResponseEntity<ApiResponse<List<EmbossFileResponse>>> getEmbossFiles() {
        return ResponseEntity.ok(ApiResponse.ok(embossService.getAllEmbossFiles()));
    }

    @GetMapping("/reject-queue")
    @Operation(summary = "Antrean Rekaman Reject Berkas Emboss")
    public ResponseEntity<ApiResponse<List<EmbossRecordResponse>>> getRejectedRecords() {
        return ResponseEntity.ok(ApiResponse.ok(embossService.getRejectedRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail Berkas Emboss")
    public ResponseEntity<ApiResponse<EmbossFileResponse>> getEmbossFile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(embossService.getEmbossFile(id)));
    }

    @GetMapping("/{id}/records")
    @Operation(summary = "Daftar Rekaman Kartu Berkas Emboss")
    public ResponseEntity<ApiResponse<List<EmbossRecordResponse>>> getEmbossRecords(
            @PathVariable Long id,
            @RequestParam(name = "status", required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(embossService.getEmbossRecords(id, status)));
    }

    public ResponseEntity<ApiResponse<List<EmbossRecordResponse>>> getEmbossRecords(Long id) {
        return getEmbossRecords(id, null);
    }

    @PutMapping("/records/{recordId}/reprocess")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER')")
    @Operation(summary = "Perbaiki Rekaman Reject Emboss")
    public ResponseEntity<ApiResponse<EmbossRecordResponse>> reprocessRecord(
            @PathVariable Long recordId,
            @RequestBody(required = false) Map<String, String> request) {
        String customerName = request != null ? request.get("customerName") : null;
        return ResponseEntity.ok(ApiResponse.ok("Rekaman berhasil diperbaiki", embossService.reprocessRecord(recordId, customerName)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER', 'REQUESTER_CABANG', 'ORDER_APPROVER')")
    @Operation(summary = "Unggah Berkas CSV Emboss Kartu ATM")
    public ResponseEntity<ApiResponse<EmbossFileResponse>> uploadEmbossFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            User user = principal != null && principal.getId() != null ? userRepository.findById(principal.getId()).orElse(null) : null;
            CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
            List<String[]> rows = reader.readAll();
            EmbossFileResponse embossFile = embossService.processEmbossData(file.getOriginalFilename(), rows, user);
            return ResponseEntity.ok(ApiResponse.ok("Berkas emboss berhasil diproses", embossFile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Gagal membaca file CSV: " + e.getMessage()));
        }
    }

    @GetMapping("/orders")
    @Operation(summary = "Daftar Order Emboss Kartu Nasabah")
    public ResponseEntity<ApiResponse<List<com.bankjatim.jims.dto.OrderResponse>>> getEmbossOrders() {
        return ResponseEntity.ok(ApiResponse.ok(embossService.getEmbossOrders()));
    }

    @PostMapping("/{id}/generate-orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER', 'REQUESTER_CABANG', 'ORDER_APPROVER')")
    @Operation(summary = "Otomatisasi Penerbitan Pesanan Cabang dari Berkas Emboss")
    public ResponseEntity<ApiResponse<Void>> generateOrders(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        try {
            User user = principal != null && principal.getId() != null ? userRepository.findById(principal.getId()).orElse(null) : null;
            embossService.generateOrdersFromEmboss(id, user);
            return ResponseEntity.ok(ApiResponse.ok("Pesanan cabang dari berkas emboss berhasil dibuat", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Gagal menerbitkan order emboss: " + e.getMessage()));
        }
    }
}
