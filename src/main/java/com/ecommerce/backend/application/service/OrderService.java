package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.support.IdempotencyKeyNormalizer;
import com.ecommerce.backend.application.support.OrderIdempotencySupport;
import com.ecommerce.backend.domain.entity.Address;
import com.ecommerce.backend.domain.entity.Order;
import com.ecommerce.backend.domain.entity.OrderIdempotencyRecord;
import com.ecommerce.backend.domain.entity.OrderItem;
import com.ecommerce.backend.domain.entity.PaymentMethod;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.infrastructure.config.EcommerceProperties;
import com.ecommerce.backend.infrastructure.exception.ConflictException;
import com.ecommerce.backend.infrastructure.exception.IdempotencyConflictException;
import com.ecommerce.backend.infrastructure.messaging.OrderCreatedEvent;
import com.ecommerce.backend.infrastructure.messaging.OrderEventPublisher;
import com.ecommerce.backend.infrastructure.repository.AddressRepository;
import com.ecommerce.backend.infrastructure.repository.OrderIdempotencyRepository;
import com.ecommerce.backend.infrastructure.repository.OrderRepository;
import com.ecommerce.backend.infrastructure.repository.PaymentMethodRepository;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ObjectProvider<OrderEventPublisher> orderEventPublisher;
    private final OrderIdempotencyRepository orderIdempotencyRepository;
    private final OrderIdempotencySupport orderIdempotencySupport;
    private final TransactionTemplate transactionTemplate;
    private final CartService cartService;
    private final EcommerceProperties ecommerceProperties;

    public OrderService(OrderRepository orderRepository,
            ProductRepository productRepository,
            AddressRepository addressRepository,
            PaymentMethodRepository paymentMethodRepository,
            ObjectProvider<OrderEventPublisher> orderEventPublisher,
            OrderIdempotencyRepository orderIdempotencyRepository,
            OrderIdempotencySupport orderIdempotencySupport,
            PlatformTransactionManager transactionManager,
            CartService cartService,
            EcommerceProperties ecommerceProperties) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.orderIdempotencyRepository = orderIdempotencyRepository;
        this.orderIdempotencySupport = orderIdempotencySupport;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.cartService = cartService;
        this.ecommerceProperties = ecommerceProperties;
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<List<OrderDto>> getUserOrders(Long userId) {
        try {
            List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            List<OrderDto> orderDtos = orders.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Orders retrieved successfully", orderDtos);
        } catch (Exception e) {
            log.error("Error retrieving orders for user {}: {}", userId, e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving orders: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<OrderDto> getOrderById(Long orderId, Long userId) {
        try {
            return orderRepository.findByIdAndUserId(orderId, userId)
                    .map(order -> BaseResponseDto.success("Order retrieved successfully", convertToDto(order)))
                    .orElse(BaseResponseDto.codedError("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        } catch (Exception e) {
            log.error("Error retrieving order {} for user {}: {}", orderId, userId, e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving order: " + e.getMessage());
        }
    }

    /**
     * @param idempotencyKey optional normalized key (controller validates length
     *                       when present)
     */
    public BaseResponseDto<OrderDto> createOrder(Long userId, CreateOrderDto createOrderDto, String idempotencyKey) {
        String key = IdempotencyKeyNormalizer.normalize(idempotencyKey);
        if (key != null) {
            Optional<Long> existing = orderIdempotencySupport.findExistingOrderId(userId, key);
            if (existing.isPresent()) {
                BaseResponseDto<OrderDto> replay = getOrderById(existing.get(), userId);
                if (replay.isSuccess()) {
                    return BaseResponseDto.success("Idempotent replay — same order as first request", replay.getData());
                }
                return replay;
            }
        }

        try {
            return transactionTemplate.execute(status -> doCreateOrder(userId, createOrderDto, key));
        } catch (IdempotencyConflictException ex) {
            log.debug("Idempotency race resolved: userId={} key={}", userId, key);
            return orderIdempotencySupport.findExistingOrderId(userId, key)
                    .flatMap(oid -> orderRepository.findByIdAndUserId(oid, userId).map(this::convertToDto))
                    .map(dto -> BaseResponseDto.success("Idempotent replay — same order as first request", dto))
                    .orElse(BaseResponseDto.error("Temporary conflict; please retry."));
        }
    }

    private BaseResponseDto<OrderDto> doCreateOrder(Long userId, CreateOrderDto createOrderDto, String idempotencyKey) {
        try {
            if (addressRepository.findByIdAndUserIdAndIsActiveTrue(createOrderDto.getShippingAddressId(), userId)
                    .isEmpty()) {
                return BaseResponseDto.codedError("ADDRESS_NOT_OWNED",
                        "Seçilen teslimat adresi bulunamadı veya hesabınıza ait değil.");
            }
            if (paymentMethodRepository.findByIdAndUserIdAndIsActiveTrue(createOrderDto.getPaymentMethodId(), userId)
                    .isEmpty()) {
                return BaseResponseDto.codedError("PAYMENT_NOT_OWNED",
                        "Seçilen ödeme yöntemi bulunamadı veya hesabınıza ait değil.");
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CreateOrderItemDto item : createOrderDto.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .filter(Product::getIsActive)
                        .orElse(null);

                if (product == null) {
                    return BaseResponseDto.codedError("PRODUCT_NOT_AVAILABLE",
                            "Ürün bulunamadı veya satışa kapalı (ID: " + item.getProductId() + ").");
                }

                if (product.getUnitInStock() < item.getQuantity()) {
                    return BaseResponseDto.codedError("STOCK_INSUFFICIENT",
                            "Yetersiz stok: " + product.getProductName() + " (istenen: " + item.getQuantity()
                                    + ", stokta: " + product.getUnitInStock() + ").");
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(item.getProductId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setUnitPrice(product.getUnitPrice());
                orderItem.setTotalPrice(product.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

                orderItems.add(orderItem);
                totalAmount = totalAmount.add(orderItem.getTotalPrice());

                product.setUnitInStock(product.getUnitInStock() - item.getQuantity());
                productRepository.saveAndFlush(product);
            }

            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setStatus("Pending");
            order.setShippingAddressId(createOrderDto.getShippingAddressId());
            order.setBillingAddressId(createOrderDto.getShippingAddressId());
            order.setPaymentMethodId(createOrderDto.getPaymentMethodId());
            order.setNotes(createOrderDto.getNotes());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setItems(orderItems);
            for (OrderItem oi : orderItems) {
                oi.setOrder(order);
            }

            Order savedOrder = orderRepository.saveAndFlush(order);

            if (idempotencyKey != null) {
                try {
                    orderIdempotencyRepository
                            .saveAndFlush(new OrderIdempotencyRecord(userId, idempotencyKey, savedOrder.getId()));
                } catch (DataIntegrityViolationException ex) {
                    throw new IdempotencyConflictException(ex);
                }
            }

            publishOrderCreatedAfterCommit(new OrderCreatedEvent(
                    savedOrder.getId(),
                    userId,
                    savedOrder.getOrderNumber(),
                    totalAmount,
                    System.currentTimeMillis()));

            cartService.clearCartForUser(userId);

            return BaseResponseDto.success("Order created successfully", convertToDto(savedOrder));
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock on product stock for user {}: {}", userId, e.getMessage());
            throw new ConflictException("Concurrent stock update. Please retry in a moment.", e);
        } catch (ConflictException | IdempotencyConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating order for user {}: {}", userId, e.getMessage(), e);
            return BaseResponseDto.codedError("ORDER_CREATE_FAILED", "Sipariş oluşturulamadı. Lütfen tekrar deneyin.");
        }
    }

    private void publishOrderCreatedAfterCommit(OrderCreatedEvent event) {
        orderEventPublisher.ifAvailable(pub -> {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        pub.publishOrderCreated(event);
                    }
                });
            } else {
                pub.publishOrderCreated(event);
            }
        });
    }

    @Transactional
    public BaseResponseDto<OrderDto> updateOrderStatus(Long orderId, UpdateOrderStatusDto updateOrderStatusDto) {
        try {
            return orderRepository.findById(orderId)
                    .map(order -> {
                        order.setStatus(updateOrderStatusDto.getStatus());
                        order.setUpdatedAt(LocalDateTime.now());
                        Order updatedOrder = orderRepository.save(order);
                        return BaseResponseDto.success("Order status updated successfully", convertToDto(updatedOrder));
                    })
                    .orElse(BaseResponseDto.error("Order not found"));
        } catch (Exception e) {
            log.error("Error updating order status for order {}: {}", orderId, e.getMessage(), e);
            return BaseResponseDto.error("Error updating order status: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDto<String> cancelOrder(Long orderId, Long userId, String reason) {
        try {
            Optional<Order> opt = orderRepository.findByIdAndUserId(orderId, userId);
            if (opt.isEmpty()) {
                return BaseResponseDto.codedError("ORDER_NOT_FOUND", "Sipariş bulunamadı.");
            }
            Order order = opt.get();
            String st = order.getStatus();
            if ("Cancelled".equals(st)) {
                return BaseResponseDto.codedError("ORDER_ALREADY_CANCELLED", "Bu sipariş zaten iptal edilmiş.");
            }
            if ("Shipped".equals(st) || "Delivered".equals(st) || "ReturnRequested".equals(st)) {
                return BaseResponseDto.codedError("ORDER_NOT_CANCELLABLE",
                        "Kargoya verilmiş, teslim edilmiş veya iade sürecindeki siparişler müşteri tarafından iptal edilemez.");
            }

            for (OrderItem item : order.getItems()) {
                productRepository.findById(item.getProductId())
                        .ifPresent(product -> {
                            product.setUnitInStock(product.getUnitInStock() + item.getQuantity());
                            productRepository.save(product);
                        });
            }

            order.setStatus("Cancelled");
            if (reason != null && !reason.isBlank()) {
                order.setCancelReason(reason.trim());
            }
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            return BaseResponseDto.success("Sipariş iptal edildi", "OK");
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock while cancelling order {}: {}", orderId, e.getMessage());
            throw new ConflictException("Concurrent stock update while cancelling. Please retry.", e);
        } catch (Exception e) {
            log.error("Error cancelling order {} for user {}: {}", orderId, userId, e.getMessage(), e);
            return BaseResponseDto.codedError("ORDER_CANCEL_FAILED", "İptal işlemi tamamlanamadı.");
        }
    }

    @Transactional
    public BaseResponseDto<OrderDto> requestReturn(Long orderId, Long userId, String reason) {
        Optional<Order> opt = orderRepository.findByIdAndUserId(orderId, userId);
        if (opt.isEmpty()) {
            return BaseResponseDto.codedError("ORDER_NOT_FOUND", "Sipariş bulunamadı.");
        }
        Order order = opt.get();
        if (!"Delivered".equals(order.getStatus())) {
            return BaseResponseDto.codedError("RETURN_NOT_ALLOWED",
                    "Yalnızca teslim edilmiş siparişler için iade talebi oluşturulabilir.");
        }
        order.setReturnReason(reason.trim());
        order.setReturnRequestedAt(LocalDateTime.now());
        order.setStatus("ReturnRequested");
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        return BaseResponseDto.success("İade talebiniz alındı", convertToDto(saved));
    }

    @Transactional
    public BaseResponseDto<OrderDto> demoAdvanceFulfillment(Long orderId, Long userId) {
        if (!ecommerceProperties.isDemoFulfillmentEnabled()) {
            return BaseResponseDto.codedError("DEMO_FULFILLMENT_DISABLED",
                    "Sipariş lojistik simülasyonu bu sunucuda kapalı (app.ecommerce.demo-fulfillment-enabled).");
        }
        Optional<Order> opt = orderRepository.findByIdAndUserId(orderId, userId);
        if (opt.isEmpty()) {
            return BaseResponseDto.codedError("ORDER_NOT_FOUND", "Sipariş bulunamadı.");
        }
        Order order = opt.get();
        String st = order.getStatus();
        switch (st) {
            case "Pending":
                order.setStatus("Processing");
                break;
            case "Processing":
                order.setStatus("Shipped");
                order.setCarrier("Yurtiçi Kargo");
                order.setTrackingNumber(
                        "TR" + String.format("%010d", ThreadLocalRandom.current().nextInt(1_000_000_000)));
                order.setShippedAt(LocalDateTime.now());
                order.setEstimatedDeliveryAt(LocalDateTime.now().plusDays(3));
                break;
            case "Shipped":
                order.setStatus("Delivered");
                break;
            default:
                return BaseResponseDto.codedError("DEMO_ADVANCE_INVALID_STATE",
                        "Bu sipariş durumu için simüle edilecek sonraki adım yok.");
        }
        order.setUpdatedAt(LocalDateTime.now());
        return BaseResponseDto.success("Sipariş durumu güncellendi (demo)", convertToDto(orderRepository.save(order)));
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<List<OrderDto>> getAllOrders(int pageNumber, int pageSize) {
        try {
            List<Order> orders = orderRepository.findAllOrderByCreatedAtDesc();
            List<OrderDto> orderDtos = orders.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Orders retrieved successfully", orderDtos);
        } catch (Exception e) {
            log.error("Error retrieving all orders: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving orders: " + e.getMessage());
        }
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setUserName(
                order.getUser() != null ? order.getUser().getFirstName() + " " + order.getUser().getLastName() : "");
        dto.setUserEmail(order.getUser() != null ? order.getUser().getEmail() : "");
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setCarrier(order.getCarrier());
        dto.setShippedAt(order.getShippedAt());
        dto.setEstimatedDeliveryAt(order.getEstimatedDeliveryAt());
        dto.setCancelReason(order.getCancelReason());
        dto.setReturnReason(order.getReturnReason());
        dto.setReturnRequestedAt(order.getReturnRequestedAt());

        Long uid = order.getUserId();
        if (order.getShippingAddressId() != null) {
            addressRepository.findById(order.getShippingAddressId())
                    .filter(a -> uid.equals(a.getUserId()))
                    .ifPresent(a -> dto.setShippingAddress(toAddressDto(a)));
        }
        if (order.getPaymentMethodId() != null) {
            paymentMethodRepository.findById(order.getPaymentMethodId())
                    .filter(pm -> uid.equals(pm.getUserId()))
                    .ifPresent(pm -> dto.setPaymentMethod(toPaymentMethodDto(pm)));
        }

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(item -> {
                        OrderItemDto itemDto = new OrderItemDto();
                        itemDto.setId(item.getId());
                        itemDto.setProductId(item.getProductId());
                        itemDto.setProductName(item.getProduct() != null ? item.getProduct().getProductName() : "");
                        itemDto.setProductImageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : null);
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setUnitPrice(item.getUnitPrice());
                        itemDto.setTotalPrice(item.getTotalPrice());
                        return itemDto;
                    })
                    .collect(Collectors.toList()));
        }

        if (ecommerceProperties.isDemoFulfillmentEnabled()) {
            String st = order.getStatus();
            if ("Pending".equals(st) || "Processing".equals(st) || "Shipped".equals(st)) {
                dto.setDemoNextAction("DEMO_ADVANCE_FULFILLMENT");
            }
        }

        return dto;
    }

    private static AddressDto toAddressDto(Address a) {
        return new AddressDto(
                a.getId(),
                a.getUserId(),
                a.getTitle(),
                a.getFullAddress(),
                a.getCity(),
                a.getDistrict(),
                a.getPostalCode(),
                a.getCountry(),
                a.getIsDefault(),
                a.getPhoneNumber(),
                a.getCreatedAt(),
                a.getUpdatedAt());
    }

    private static PaymentMethodDto toPaymentMethodDto(PaymentMethod pm) {
        return new PaymentMethodDto(
                pm.getId(),
                pm.getUserId(),
                pm.getType(),
                pm.getCardHolderName(),
                maskCardForDisplay(pm.getCardNumber()),
                pm.getExpiryMonth(),
                pm.getExpiryYear(),
                pm.getBankName(),
                pm.getAccountNumber(),
                pm.getAccountHolderName(),
                pm.getIsDefault(),
                pm.getIsActive(),
                pm.getCreatedAt(),
                pm.getUpdatedAt());
    }

    private static String maskCardForDisplay(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        if (cardNumber.startsWith("****")) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
