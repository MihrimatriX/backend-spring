package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CreateOrderDto;
import com.ecommerce.backend.application.dto.CreateOrderItemDto;
import com.ecommerce.backend.application.dto.OrderDto;
import com.ecommerce.backend.application.service.OrderService;
import com.ecommerce.backend.infrastructure.security.JwtUtil;
import com.ecommerce.backend.infrastructure.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tam Spring Boot bağlamı yerine {@link MockMvcBuilders#standaloneSetup(Object...)} ile yalnızca
 * {@link OrderController} + {@link GlobalExceptionHandler} test edilir (diğer controller’lar yüklenmez).
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerWebMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    @Mock
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService, jwtUtil))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rejectsShortIdempotencyKey() throws Exception {
        when(jwtUtil.extractUserId(any(HttpServletRequest.class))).thenReturn(1L);

        String body = objectMapper.writeValueAsString(sampleCreateOrderDto());

        mockMvc.perform(post("/api/order")
                        .header("Idempotency-Key", "short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forwardsNormalizedIdempotencyKeyToService() throws Exception {
        when(jwtUtil.extractUserId(any(HttpServletRequest.class))).thenReturn(42L);
        OrderDto dto = new OrderDto();
        dto.setId(99L);
        when(orderService.createOrder(eq(42L), any(), eq("MY-LONG-KEY-001")))
                .thenReturn(BaseResponseDto.success("Order created successfully", dto));

        String body = objectMapper.writeValueAsString(sampleCreateOrderDto());

        mockMvc.perform(post("/api/order")
                        .header("Idempotency-Key", "  MY-LONG-KEY-001  ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(orderService).createOrder(eq(42L), any(), eq("MY-LONG-KEY-001"));
    }

    private static CreateOrderDto sampleCreateOrderDto() {
        CreateOrderItemDto line = new CreateOrderItemDto();
        line.setProductId(1L);
        line.setQuantity(1);
        CreateOrderDto dto = new CreateOrderDto();
        dto.setShippingAddressId(10L);
        dto.setPaymentMethodId(20L);
        dto.setItems(List.of(line));
        return dto;
    }
}
