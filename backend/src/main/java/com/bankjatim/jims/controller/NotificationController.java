package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.dto.NotificationResponse;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoint notifikasi pengguna")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Daftar Notifikasi Pengguna")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotifications(principal)));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Tandai Semua Notifikasi Dibaca")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Semua notifikasi ditandai dibaca", null));
    }
}
