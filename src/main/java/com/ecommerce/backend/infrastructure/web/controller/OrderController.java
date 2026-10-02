package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CancelOrderRequest;
import com.ecommerce.backend.application.dto.CreateOrderDto;
import com.ecommerce.backend.application.dto.OrderDto;
import com.ecommerce.backend.application.dto.ReturnRequestDto;
import com.ecommerce.backend.application.dto.UpdateOrderStatusDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.service.OrderService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Siparişler (§4.10)")
public class OrderController {

    private static final int MIN_KEY = 8;
    private static final int MAX_KEY = 128;

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<OrderDto>>> getMyOrders() {
        return ApiResponses.ok("Siparişler listelendi", orderService.getUserOrders(currentUserService.requireUserId()));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<List<OrderDto>>> getAll(@RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Siparişler listelendi",
                orderService.getAllOrders(ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 20)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<OrderDto>> getById(@PathVariable Long id) {
        return ApiResponses.ok("Sipariş getirildi", orderService.getOrder(id, currentUserService.requireUserId()));
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto<OrderDto>> create(@Valid @RequestBody CreateOrderDto request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String key = idempotencyKey == null ? null : idempotencyKey.trim();
        if (key != null && !key.isEmpty() && (key.length() < MIN_KEY || key.length() > MAX_KEY)) {
            throw ApiException.badRequest("IDEMPOTENCY_KEY_INVALID",
                    "Idempotency-Key 8 ile 128 karakter arasında olmalıdır.");
        }
        OrderService.CreateResult result = orderService.createOrder(currentUserService.requireUserId(), request,
                key == null || key.isEmpty() ? null : key);
        return result.replay()
                ? ApiResponses.ok("Idempotent replay — same order as first request", result.order())
                : ApiResponses.created("Siparişiniz alındı", result.order());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BaseResponseDto<String>> cancel(@PathVariable Long id,
            @Valid @RequestBody(required = false) CancelOrderRequest body) {
        orderService.cancelOrder(id, currentUserService.requireUserId(), body == null ? null : body.getReason());
        return ApiResponses.ok("Sipariş iptal edildi", "Sipariş iptal edildi");
    }

    @PostMapping("/{id}/return-request")
    public ResponseEntity<BaseResponseDto<OrderDto>> requestReturn(@PathVariable Long id,
            @Valid @RequestBody ReturnRequestDto body) {
        return ApiResponses.ok("İade talebiniz alındı",
                orderService.requestReturn(id, currentUserService.requireUserId(), body.getReason()));
    }

    @PostMapping("/{id}/demo/advance-fulfillment")
    public ResponseEntity<BaseResponseDto<OrderDto>> demoAdvance(@PathVariable Long id) {
        return ApiResponses.ok("Sipariş durumu güncellendi (demo)",
                orderService.demoAdvanceFulfillment(id, currentUserService.requireUserId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<OrderDto>> updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDto body) {
        return ApiResponses.ok("Sipariş durumu güncellendi", orderService.updateOrderStatus(id, body.getStatus()));
    }
}
