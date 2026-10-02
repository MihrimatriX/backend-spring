package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.AddressDto;
import com.ecommerce.backend.application.dto.CreateOrderDto;
import com.ecommerce.backend.application.dto.CreateOrderItemDto;
import com.ecommerce.backend.application.dto.OrderDto;
import com.ecommerce.backend.application.dto.OrderItemDto;
import com.ecommerce.backend.application.dto.PaymentMethodDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.support.AfterCommit;
import com.ecommerce.backend.application.support.CheckoutPricing;
import com.ecommerce.backend.application.support.IdempotencyKeyNormalizer;
import com.ecommerce.backend.application.support.OrderIdempotencySupport;
import com.ecommerce.backend.application.support.PaymentCards;
import com.ecommerce.backend.application.support.ProductCacheInvalidator;
import com.ecommerce.backend.domain.entity.Order;
import com.ecommerce.backend.domain.entity.OrderIdempotencyRecord;
import com.ecommerce.backend.domain.entity.OrderItem;
import com.ecommerce.backend.domain.entity.PaymentMethod;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.ShoppingCartItem;
import com.ecommerce.backend.infrastructure.config.CheckoutProperties;
import com.ecommerce.backend.infrastructure.config.EcommerceProperties;
import com.ecommerce.backend.infrastructure.messaging.OrderCreatedEvent;
import com.ecommerce.backend.infrastructure.messaging.OrderEventPublisher;
import com.ecommerce.backend.infrastructure.repository.AddressRepository;
import com.ecommerce.backend.infrastructure.repository.OrderIdempotencyRepository;
import com.ecommerce.backend.infrastructure.repository.OrderRepository;
import com.ecommerce.backend.infrastructure.repository.PaymentMethodRepository;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sipariş akışı — docs/API_CONTRACT.md §4.10, §5.2–§5.5.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    public static final List<String> STATUSES = List.of("Pending", "Processing", "Shipped", "Delivered", "Cancelled",
            "ReturnRequested", "Returned");
    private static final Set<String> CANCELLABLE = Set.of("Pending", "Processing");
    private static final String DEFAULT_CARRIER = "Yurtiçi Kargo";

    /** Sipariş oluşturma sonucu; {@code replay} = aynı Idempotency-Key ile tekrar. */
    public record CreateResult(OrderDto order, boolean replay) {
    }

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderIdempotencyRepository orderIdempotencyRepository;
    private final OrderIdempotencySupport orderIdempotencySupport;
    private final CartService cartService;
    private final CheckoutPricing checkoutPricing;
    private final CheckoutProperties checkoutProperties;
    private final EcommerceProperties ecommerceProperties;
    private final UserNotifier userNotifier;
    private final ProductCacheInvalidator productCacheInvalidator;
    private final ObjectProvider<OrderEventPublisher> orderEventPublisher;

    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findDetailedByUserId(userId).stream().map(this::toDto).toList();
    }

    public OrderDto getOrder(Long orderId, Long userId) {
        return toDto(requireOwned(orderId, userId));
    }

    public List<OrderDto> getAllOrders(int pageNumber, int pageSize) {
        List<Long> ids = orderRepository.findIdsNewestFirst(PageRequest.of(pageNumber - 1, pageSize));
        if (ids.isEmpty()) {
            return List.of();
        }
        return orderRepository.findDetailedByIdIn(ids).stream().map(this::toDto).toList();
    }

    /** §5.2 — tek transaction; hata durumunda hiçbir değişiklik kalıcı olmaz. */
    @Transactional
    public CreateResult createOrder(Long userId, CreateOrderDto request, String idempotencyKey) {
        String key = IdempotencyKeyNormalizer.normalize(idempotencyKey);
        if (key != null) {
            Optional<Long> existing = orderIdempotencySupport.findExistingOrderId(userId, key);
            if (existing.isPresent()) {
                return new CreateResult(getOrder(existing.get(), userId), true);
            }
        }

        Map<Long, Integer> requested = new TreeMap<>();
        for (CreateOrderItemDto item : request.getItems()) {
            if (item != null && item.getProductId() != null && item.getQuantity() != null) {
                requested.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            }
        }
        if (requested.isEmpty()) {
            throw ApiException.badRequest("EMPTY_ORDER", "Sepette ürün yok.");
        }

        var address = addressRepository.findByIdAndUserIdAndIsActiveTrue(request.getShippingAddressId(), userId)
                .orElseThrow(() -> ApiException.badRequest("INVALID_ADDRESS",
                        "Teslimat adresi geçersiz veya size ait değil."));
        PaymentMethod payment = paymentMethodRepository
                .findByIdAndUserIdAndIsActiveTrue(request.getPaymentMethodId(), userId)
                .orElseThrow(() -> ApiException.badRequest("INVALID_PAYMENT",
                        "Ödeme yöntemi geçersiz veya size ait değil."));

        checkAgainstCart(userId, requested);

        List<OrderItem> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : requested.entrySet()) {
            Product product = productRepository.findById(e.getKey())
                    .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                    .orElseThrow(() -> ApiException.badRequest("CHECKOUT_FAILED",
                            "Bir ürün artık satışta değil veya bulunamadı."));
            int qty = e.getValue();
            if (product.getUnitInStock() < qty) {
                throw ApiException.badRequest("CHECKOUT_FAILED",
                        "Yetersiz stok: " + product.getProductName() + ". Miktarı azaltın veya sepetten çıkarın.");
            }
            BigDecimal unit = CheckoutPricing.salePrice(product.getUnitPrice(), product.getDiscount());
            OrderItem line = new OrderItem();
            line.setProductId(product.getId());
            line.setProduct(product);
            line.setQuantity(qty);
            line.setUnitPrice(unit);
            line.setTotalPrice(CheckoutPricing.lineTotal(unit, qty));
            lines.add(line);
            subtotal = subtotal.add(line.getTotalPrice());
            product.setUnitInStock(product.getUnitInStock() - qty);
        }

        CheckoutPricing.Quote quote = checkoutPricing.quote(subtotal, false);
        authorizePayment(payment);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        order.setStatus("Pending");
        order.setSubtotalAmount(quote.subtotal());
        order.setShippingFee(quote.shippingFee());
        order.setTotalAmount(quote.grandTotal());
        order.setShippingAddressId(address.getId());
        order.setBillingAddressId(address.getId());
        order.setPaymentMethodId(payment.getId());
        order.setNotes(request.getNotes());
        for (OrderItem line : lines) {
            line.setOrder(order);
        }
        order.setItems(lines);
        Order saved = orderRepository.saveAndFlush(order);

        if (key != null) {
            orderIdempotencyRepository.save(new OrderIdempotencyRecord(userId, key, saved.getId()));
        }
        cartService.clearCart(userId);
        userNotifier.notify(userId, "Sipariş alındı", "#" + saved.getOrderNumber()
                + " siparişiniz oluşturuldu. Tutar: " + saved.getTotalAmount().setScale(2).toPlainString(),
                UserNotifier.TYPE_ORDER, "/orders/" + saved.getId());
        productCacheInvalidator.evictAfterCommit();
        OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), userId, saved.getOrderNumber(),
                saved.getTotalAmount(), System.currentTimeMillis());
        orderEventPublisher.ifAvailable(pub -> AfterCommit.run(() -> pub.publishOrderCreated(event)));

        return new CreateResult(getOrder(saved.getId(), userId), false);
    }

    /** §5.3 */
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = requireOwned(orderId, userId);
        if (!CANCELLABLE.contains(order.getStatus())) {
            throw ApiException.badRequest("CANCEL_NOT_ALLOWED", "Bu sipariş iptal edilemez.");
        }
        restoreStock(order);
        if (reason != null && !reason.isBlank()) {
            order.setCancelReason(reason.trim());
        }
        changeStatus(order, "Cancelled");
    }

    @Transactional
    public OrderDto requestReturn(Long orderId, Long userId, String reason) {
        Order order = requireOwned(orderId, userId);
        if (!"Delivered".equals(order.getStatus())) {
            throw ApiException.badRequest("RETURN_NOT_ALLOWED",
                    "Yalnızca teslim edilmiş siparişler için iade talebi oluşturulabilir.");
        }
        order.setReturnReason(reason.trim());
        order.setReturnRequestedAt(LocalDateTime.now());
        changeStatus(order, "ReturnRequested");
        return toDto(order);
    }

    /** §5.5 */
    @Transactional
    public OrderDto demoAdvanceFulfillment(Long orderId, Long userId) {
        if (!ecommerceProperties.isDemoFulfillmentEnabled()) {
            throw ApiException.notFound("DEMO_FULFILLMENT_DISABLED",
                    "Sipariş lojistik simülasyonu bu sunucuda kapalı.");
        }
        Order order = requireOwned(orderId, userId);
        String next = switch (order.getStatus()) {
            case "Pending" -> "Processing";
            case "Processing" -> "Shipped";
            case "Shipped" -> "Delivered";
            default -> throw ApiException.badRequest("DEMO_ADVANCE_INVALID_STATE",
                    "Bu sipariş durumu için simüle edilecek sonraki adım yok.");
        };
        changeStatus(order, next);
        return toDto(order);
    }

    /** §5.4 */
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String requestedStatus) {
        String next = STATUSES.stream().filter(s -> s.equalsIgnoreCase(requestedStatus == null ? "" : requestedStatus.trim()))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("INVALID_STATUS",
                        "Geçersiz durum. Kullanın: " + String.join(", ", STATUSES) + "."));
        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> ApiException.notFound("ORDER_NOT_FOUND", "Sipariş bulunamadı"));
        String prev = order.getStatus();
        if (("Cancelled".equals(next) && CANCELLABLE.contains(prev))
                || ("Returned".equals(next) && !"Returned".equals(prev))) {
            restoreStock(order);
        }
        changeStatus(order, next);
        return toDto(order);
    }

    private void changeStatus(Order order, String next) {
        String prev = order.getStatus();
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(next);
        if ("Shipped".equals(next)) {
            order.setShippedAt(now);
            if (order.getCarrier() == null) {
                order.setCarrier(DEFAULT_CARRIER);
            }
            if (order.getTrackingNumber() == null) {
                order.setTrackingNumber(
                        "TR" + String.format("%010d", ThreadLocalRandom.current().nextLong(10_000_000_000L)));
            }
            order.setEstimatedDeliveryAt(now.plusDays(3));
        } else if ("Delivered".equals(next)) {
            order.setDeliveredAt(now);
        }
        order.setUpdatedAt(now);
        orderRepository.save(order);
        if (!next.equals(prev)) {
            notifyStatusChange(order, next);
        }
    }

    private void notifyStatusChange(Order order, String status) {
        String title = switch (status) {
            case "Processing" -> "Sipariş hazırlanıyor";
            case "Shipped" -> "Sipariş kargoya verildi";
            case "Delivered" -> "Sipariş teslim edildi";
            case "Cancelled" -> "Sipariş iptal edildi";
            case "ReturnRequested" -> "İade talebi alındı";
            case "Returned" -> "İade tamamlandı";
            default -> null;
        };
        if (title == null) {
            return;
        }
        String verb = switch (status) {
            case "Processing" -> "hazırlanıyor";
            case "Shipped" -> "kargoya verildi";
            case "Delivered" -> "teslim edildi";
            case "Cancelled" -> "iptal edildi";
            case "ReturnRequested" -> "için iade talebi alındı";
            default -> "iade süreci tamamlandı";
        };
        userNotifier.notify(order.getUserId(), title, "#" + order.getOrderNumber() + " siparişiniz " + verb + ".",
                UserNotifier.TYPE_ORDER, "/orders/" + order.getId());
    }

    /** Sunucu sepeti doluysa istek sepetle birebir aynı olmalı (§5.2 adım 5). */
    private void checkAgainstCart(Long userId, Map<Long, Integer> requested) {
        List<ShoppingCartItem> cartLines = shoppingCartRepository.findByUserId(userId)
                .map(c -> c.getItems().stream()
                        .filter(i -> i.getQuantity() != null && i.getQuantity() > 0)
                        .filter(i -> Boolean.TRUE.equals(i.getProduct().getIsActive()))
                        .toList())
                .orElse(List.of());
        if (cartLines.isEmpty()) {
            return;
        }
        boolean unavailable = cartLines.stream().anyMatch(i -> i.getProduct().getUnitInStock() <= 0
                || i.getProduct().getUnitInStock() < i.getQuantity());
        if (unavailable) {
            throw ApiException.badRequest("CART_UNAVAILABLE",
                    "Sepetinizde stokta olmayan veya miktarı aşan ürün var. Sepeti güncelleyip tekrar deneyin.");
        }
        Map<Long, Integer> fromCart = new TreeMap<>();
        cartLines.forEach(i -> fromCart.merge(i.getProduct().getId(), i.getQuantity(), Integer::sum));
        if (!fromCart.equals(requested)) {
            throw ApiException.badRequest("CART_MISMATCH",
                    "Sepet ile ödeme özeti uyuşmuyor. Sayfayı yenileyip tekrar deneyin.");
        }
    }

    private void authorizePayment(PaymentMethod payment) {
        if (PaymentCards.isExpired(payment.getExpiryMonth(), payment.getExpiryYear(), LocalDate.now(ZoneOffset.UTC))) {
            throw ApiException.badRequest("CHECKOUT_FAILED",
                    "Kartın son kullanma tarihi geçmiş. Lütfen başka bir kart seçin.");
        }
        int rate = Math.max(0, Math.min(100, checkoutProperties.getSimulatedPaymentDeclinePercent()));
        if (rate > 0 && ThreadLocalRandom.current().nextInt(100) < rate) {
            throw ApiException.badRequest("CHECKOUT_FAILED",
                    "Ödeme sağlayıcı işlemi onaylamadı. Bankanızı arayın veya farklı kart deneyin.");
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem line : order.getItems()) {
            productRepository.findById(line.getProductId())
                    .ifPresent(p -> p.setUnitInStock(p.getUnitInStock() + line.getQuantity()));
        }
        productCacheInvalidator.evictAfterCommit();
    }

    private Order requireOwned(Long orderId, Long userId) {
        return orderRepository.findDetailedById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("ORDER_NOT_FOUND", "Sipariş bulunamadı"));
    }

    private static String generateOrderNumber() {
        return "ORD-" + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setUserId(o.getUserId());
        dto.setUserName(o.getUser() == null ? "" : (o.getUser().getFirstName() + " " + o.getUser().getLastName()).trim());
        dto.setUserEmail(o.getUser() == null ? "" : o.getUser().getEmail());
        Map<Long, OrderItemDto> items = new LinkedHashMap<>();
        for (OrderItem i : o.getItems()) {
            items.put(i.getId(), new OrderItemDto(i.getId(), i.getProductId(),
                    i.getProduct() == null ? "" : i.getProduct().getProductName(),
                    i.getProduct() == null ? null : i.getProduct().getImageUrl(),
                    i.getQuantity(), i.getUnitPrice(), i.getTotalPrice()));
        }
        dto.setItems(new ArrayList<>(items.values()));
        dto.setSubtotalAmount(o.getSubtotalAmount());
        dto.setShippingFee(o.getShippingFee());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setStatus(o.getStatus());
        dto.setNotes(o.getNotes());
        dto.setShippingAddress(o.getShippingAddress() == null ? null : AddressDto.from(o.getShippingAddress()));
        dto.setBillingAddress(o.getBillingAddress() == null ? null : AddressDto.from(o.getBillingAddress()));
        dto.setPaymentMethod(o.getPaymentMethod() == null ? null : PaymentMethodDto.from(o.getPaymentMethod()));
        dto.setTrackingNumber(o.getTrackingNumber());
        dto.setCarrier(o.getCarrier());
        dto.setShippedAt(o.getShippedAt());
        dto.setDeliveredAt(o.getDeliveredAt());
        dto.setEstimatedDeliveryAt(o.getEstimatedDeliveryAt());
        dto.setCancelReason(o.getCancelReason());
        dto.setReturnReason(o.getReturnReason());
        dto.setReturnRequestedAt(o.getReturnRequestedAt());
        if (ecommerceProperties.isDemoFulfillmentEnabled() && CANCELLABLE.contains(o.getStatus())
                || ecommerceProperties.isDemoFulfillmentEnabled() && "Shipped".equals(o.getStatus())) {
            dto.setDemoNextAction("DEMO_ADVANCE_FULFILLMENT");
        }
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());
        return dto;
    }
}
