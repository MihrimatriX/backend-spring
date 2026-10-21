package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CancelOrderRequest;
import com.ecommerce.backend.application.dto.CreateOrderDto;
import com.ecommerce.backend.application.dto.OrderDto;
import com.ecommerce.backend.application.dto.ReturnRequestDto;
import com.ecommerce.backend.application.dto.UpdateOrderStatusDto;
import com.ecommerce.backend.application.service.OrderService;
import com.ecommerce.backend.application.support.IdempotencyKeyNormalizer;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Siparişler — docs/API_CONTRACT.md §4.10 (admin uçları SecurityConfig'te). */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and tracking")
public class OrderController {

    private static final int ADMIN_PAGE_SIZE = 20;

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Get my orders", description = "Newest first")
    public ResponseEntity<BaseResponseDto<List<OrderDto>>> getUserOrders() {
        return ApiResponses.ok("Orders retrieved successfully",
                orderService.getUserOrders(currentUserService.requireUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my order by ID")
    public ResponseEntity<BaseResponseDto<OrderDto>> getOrder(@PathVariable Long id) {
        return ApiResponses.ok("Order retrieved successfully",
                orderService.getOrder(id, currentUserService.requireUserId()));
    }

    @PostMapping
    @Operation(summary = "Place order", description = "Optional Idempotency-Key header (8-128 chars)")
    public ResponseEntity<BaseResponseDto<OrderDto>> createOrder(@Valid @RequestBody CreateOrderDto dto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Long userId = currentUserService.requireUserId();
        String key = IdempotencyKeyNormalizer.normalizeAndValidate(idempotencyKey);
        OrderService.PlacedOrder placed = orderService.createOrder(userId, dto, key);
        return placed.replayed()
                ? ApiResponses.ok(OrderService.REPLAY_MESSAGE, placed.order())
                : ApiResponses.created("Siparişiniz alındı", placed.order());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel my order", description = "Only Pending/Processing; stock is restored")
    public ResponseEntity<BaseResponseDto<String>> cancelOrder(@PathVariable Long id,
            @Valid @RequestBody(required = false) CancelOrderRequest body) {
        String message = orderService.cancelOrder(id, currentUserService.requireUserId(),
                body == null ? null : body.getReason());
        return ApiResponses.ok(message, message);
    }

    @PostMapping("/{id}/return-request")
    @Operation(summary = "Request a return", description = "Only Delivered orders")
    public ResponseEntity<BaseResponseDto<OrderDto>> requestReturn(@PathVariable Long id,
            @Valid @RequestBody ReturnRequestDto body) {
        return ApiResponses.ok("İade talebiniz alındı",
                orderService.requestReturn(id, currentUserService.requireUserId(), body.getReason()));
    }

    @PostMapping("/{id}/demo/advance-fulfillment")
    @Operation(summary = "Demo: advance fulfillment", description = "Pending → Processing → Shipped → Delivered")
    public ResponseEntity<BaseResponseDto<OrderDto>> advanceDemoFulfillment(@PathVariable Long id) {
        return ApiResponses.ok("Sipariş durumu güncellendi",
                orderService.advanceDemoFulfillment(id, currentUserService.requireUserId()));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Admin: update order status")
    public ResponseEntity<BaseResponseDto<OrderDto>> updateOrderStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDto dto) {
        return ApiResponses.ok("Sipariş durumu güncellendi", orderService.updateOrderStatus(id, dto));
    }

    @GetMapping("/admin")
    @Operation(summary = "Admin: all orders", description = "Newest first")
    public ResponseEntity<BaseResponseDto<List<OrderDto>>> getAllOrders(
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Orders retrieved successfully", orderService.getAllOrders(
                ApiResponses.page(pageNumber), ApiResponses.size(pageSize, ADMIN_PAGE_SIZE)));
    }
}
