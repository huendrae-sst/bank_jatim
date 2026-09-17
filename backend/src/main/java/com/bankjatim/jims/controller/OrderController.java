package com.bankjatim.jims.controller;

import com.bankjatim.jims.common.ApiResponse;
import com.bankjatim.jims.common.PageResponse;
import com.bankjatim.jims.domain.Order;
import com.bankjatim.jims.domain.User;
import com.bankjatim.jims.dto.OrderRequest;
import com.bankjatim.jims.dto.OrderResponse;
import com.bankjatim.jims.repository.UserRepository;
import com.bankjatim.jims.security.UserPrincipal;
import com.bankjatim.jims.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Branch Orders", description = "Endpoints untuk Permintaan Kebutuhan Barang Cabang dan Approval Berjenjang")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Daftar Pesanan Cabang")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @RequestParam(required = false) Long organizationId,
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<OrderResponse> page = orderService.getOrders(principal, organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail Pesanan Cabang")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'REQUESTER_CABANG', 'ORDER_REQUESTER')")
    @Operation(summary = "Buat Pesanan Cabang")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        OrderResponse order = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Pesanan berhasil dibuat", order));
    }

    @GetMapping("/approvals")
    @Operation(summary = "Daftar Antrean Approval Pesanan")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getApprovals() {
        List<Order> list = orderService.getPendingApprovals();
        return ResponseEntity.ok(ApiResponse.ok(list.stream().map(OrderResponse::from).toList()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORDER_APPROVER')")
    @Operation(summary = "Approve Pesanan Cabang")
    public ResponseEntity<ApiResponse<OrderResponse>> approveOrder(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.getReferenceById(principal.getId());
        Order order = orderService.approveOrder(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Pesanan berhasil disetujui", OrderResponse.from(order)));
    }
}
