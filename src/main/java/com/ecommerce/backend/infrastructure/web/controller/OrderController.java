package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CancelOrderRequest;
import com.ecommerce.backend.application.dto.CreateOrderDto;
import com.ecommerce.backend.application.dto.OrderDto;
import com.ecommerce.backend.application.dto.ReturnRequestDto;
import com.ecommerce.backend.application.dto.UpdateOrderStatusDto;
import com.ecommerce.backend.application.service.OrderService;
import com.ecommerce.backend.infrastructure.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<OrderDto>>> getUserOrders(HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<List<OrderDto>> response = orderService.getUserOrders(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<OrderDto>> getOrderById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<OrderDto> response = orderService.getOrderById(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if ("ORDER_NOT_FOUND".equals(response.getCode())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping
    public ResponseEntity<BaseResponseDto<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderDto createOrderDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        if (idempotencyKey != null) {
            String trimmed = idempotencyKey.trim();
            if (trimmed.length() > 128) {
                return ResponseEntity.badRequest()
                        .body(BaseResponseDto.codedError("IDEMPOTENCY_KEY_INVALID",
                                "Idempotency-Key en fazla 128 karakter olabilir"));
            }
            if (!trimmed.isEmpty() && trimmed.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(BaseResponseDto.codedError("IDEMPOTENCY_KEY_INVALID",
                                "Idempotency-Key dolu ise en az 8 karakter olmalıdır"));
            }
            idempotencyKey = trimmed.isEmpty() ? null : trimmed;
        }
        BaseResponseDto<OrderDto> response = orderService.createOrder(userId, createOrderDto, idempotencyKey);
        if (response.isSuccess()) {
            if (response.getMessage() != null && response.getMessage().startsWith("Idempotent replay")) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(orderCreateErrorStatus(response)).body(response);
    }

    private static HttpStatus orderCreateErrorStatus(BaseResponseDto<?> r) {
        String c = r.getCode();
        if ("STOCK_INSUFFICIENT".equals(c)) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<OrderDto>> updateOrderStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDto updateOrderStatusDto) {
        BaseResponseDto<OrderDto> response = orderService.updateOrderStatus(id, updateOrderStatusDto);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BaseResponseDto<String>> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid CancelOrderRequest body,
            HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        String reason = body != null ? body.getReason() : null;
        BaseResponseDto<String> response = orderService.cancelOrder(id, userId, reason);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        HttpStatus st = "ORDER_NOT_FOUND".equals(response.getCode()) ? HttpStatus.NOT_FOUND
                : "ORDER_NOT_CANCELLABLE".equals(response.getCode())
                        || "ORDER_ALREADY_CANCELLED".equals(response.getCode())
                                ? HttpStatus.CONFLICT
                                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(response);
    }

    @PostMapping("/{id}/return-request")
    public ResponseEntity<BaseResponseDto<OrderDto>> requestReturn(
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequestDto body,
            HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<OrderDto> response = orderService.requestReturn(id, userId, body.getReason());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        HttpStatus st = "ORDER_NOT_FOUND".equals(response.getCode()) ? HttpStatus.NOT_FOUND
                : "RETURN_NOT_ALLOWED".equals(response.getCode()) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(response);
    }

    @PostMapping("/{id}/demo/advance-fulfillment")
    public ResponseEntity<BaseResponseDto<OrderDto>> demoAdvanceFulfillment(@PathVariable Long id,
            HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(request);
        BaseResponseDto<OrderDto> response = orderService.demoAdvanceFulfillment(id, userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        HttpStatus st = "DEMO_FULFILLMENT_DISABLED".equals(response.getCode()) ? HttpStatus.NOT_FOUND
                : "ORDER_NOT_FOUND".equals(response.getCode()) ? HttpStatus.NOT_FOUND
                        : "DEMO_ADVANCE_INVALID_STATE".equals(response.getCode()) ? HttpStatus.CONFLICT
                                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<List<OrderDto>>> getAllOrders(
            @RequestParam(defaultValue = "1") int pageNumber, @RequestParam(defaultValue = "10") int pageSize) {
        BaseResponseDto<List<OrderDto>> response = orderService.getAllOrders(pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }
}
