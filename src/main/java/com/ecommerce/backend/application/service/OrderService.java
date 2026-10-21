package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.AddressDto;
import com.ecommerce.backend.application.dto.CartDto;
import com.ecommerce.backend.application.dto.CartItemDto;
import com.ecommerce.backend.application.dto.CreateOrderDto;
import com.ecommerce.backend.application.dto.CreateOrderItemDto;
import com.ecommerce.backend.application.dto.OrderDto;
import com.ecommerce.backend.application.dto.OrderItemDto;
import com.ecommerce.backend.application.dto.PaymentMethodDto;
import com.ecommerce.backend.application.dto.UpdateOrderStatusDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.application.support.AfterCommit;
import com.ecommerce.backend.application.support.CheckoutPricing;
import com.ecommerce.backend.application.support.OrderIdempotencySupport;
import com.ecommerce.backend.application.support.PaymentCards;
import com.ecommerce.backend.application.support.ProductCacheInvalidator;
import com.ecommerce.backend.domain.entity.Address;
import com.ecommerce.backend.domain.entity.Order;
import com.ecommerce.backend.domain.entity.OrderIdempotencyRecord;
import com.ecommerce.backend.domain.entity.OrderItem;
import com.ecommerce.backend.domain.entity.PaymentMethod;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.config.CheckoutProperties;
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
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/** Siparişler (docs/API_CONTRACT.md §4.10, §5.2–5.5). */
@Service
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    public static final String REPLAY_MESSAGE = "Idempotent replay — same order as first request";

    static final String PENDING = "Pending";
    static final String PROCESSING = "Processing";
    static final String SHIPPED = "Shipped";
    static final String DELIVERED = "Delivered";
    static final String CANCELLED = "Cancelled";
    static final String RETURN_REQUESTED = "ReturnRequested";
    static final String RETURNED = "Returned";
    static final List<String> STATUSES = List.of(PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED,
            RETURN_REQUESTED, RETURNED);

    private static final String DEMO_NEXT_ACTION = "DEMO_ADVANCE_FULFILLMENT";
    private static final String DEFAULT_CARRIER = "Yurtiçi Kargo";
    private static final int ESTIMATED_DELIVERY_DAYS = 3;
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final OrderIdempotencyRepository orderIdempotencyRepository;
    private final OrderIdempotencySupport orderIdempotencySupport;
    private final ObjectProvider<OrderEventPublisher> orderEventPublisher;
    private final CartService cartService;
    private final CheckoutPricing pricing;
    private final CheckoutProperties checkoutProperties;
    private final EcommerceProperties ecommerceProperties;
    private final UserNotifier userNotifier;
    private final ProductCacheInvalidator productCacheInvalidator;
    private final TransactionTemplate transactionTemplate;

    public OrderService(OrderRepository orderRepository,
            ProductRepository productRepository,
            AddressRepository addressRepository,
            PaymentMethodRepository paymentMethodRepository,
            UserRepository userRepository,
            OrderIdempotencyRepository orderIdempotencyRepository,
            OrderIdempotencySupport orderIdempotencySupport,
            ObjectProvider<OrderEventPublisher> orderEventPublisher,
            CartService cartService,
            CheckoutPricing pricing,
            CheckoutProperties checkoutProperties,
            EcommerceProperties ecommerceProperties,
            UserNotifier userNotifier,
            ProductCacheInvalidator productCacheInvalidator,
            PlatformTransactionManager transactionManager) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
        this.orderIdempotencyRepository = orderIdempotencyRepository;
        this.orderIdempotencySupport = orderIdempotencySupport;
        this.orderEventPublisher = orderEventPublisher;
        this.cartService = cartService;
        this.pricing = pricing;
        this.checkoutProperties = checkoutProperties;
        this.ecommerceProperties = ecommerceProperties;
        this.userNotifier = userNotifier;
        this.productCacheInvalidator = productCacheInvalidator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /** Oluşturulan veya (aynı Idempotency-Key ile) daha önce oluşturulmuş sipariş. */
    public record PlacedOrder(OrderDto order, boolean replayed) {
    }

    /** Kullanıcının siparişleri, en yeni önce. */
    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findDetailedByUserId(userId).stream().map(this::toDto).toList();
    }

    /** Başkasının siparişi de 404 döner. */
    public OrderDto getOrder(Long orderId, Long userId) {
        return toDto(ownedOrder(orderId, userId));
    }

    /** Yönetici listesi, en yeni önce (düz dizi). */
    public List<OrderDto> getAllOrders(int pageNumber, int pageSize) {
        List<Long> ids = orderRepository.findIdsNewestFirst(PageRequest.of(pageNumber - 1, pageSize));
        return ids.isEmpty() ? List.of() : orderRepository.findDetailedByIdIn(ids).stream().map(this::toDto).toList();
    }

    /**
     * §5.2. {@code idempotencyKey} controller'da normalize edilmiş gelir. Sipariş kendi transaction'ında
     * yazılır; iyimser kilit çakışması commit'te yakalanıp 409'a çevrilir.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PlacedOrder createOrder(Long userId, CreateOrderDto request, String idempotencyKey) {
        if (idempotencyKey != null) {
            var existing = orderIdempotencySupport.findExistingOrderId(userId, idempotencyKey);
            if (existing.isPresent()) {
                return new PlacedOrder(getOrder(existing.get(), userId), true);
            }
        }
        Long orderId;
        try {
            orderId = transactionTemplate.execute(status -> placeOrder(userId, request, idempotencyKey));
        } catch (IdempotencyConflictException ex) {
            // Aynı anahtarla paralel gelen iki istekten diğeri kazandı: onun siparişini döndür.
            Long winner = orderIdempotencySupport.findExistingOrderId(userId, idempotencyKey)
                    .orElseThrow(() -> ex);
            return new PlacedOrder(getOrder(winner, userId), true);
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.warn("Eşzamanlı stok güncellemesi (kullanıcı {}): {}", userId, ex.getMessage());
            throw new ConflictException("Kayıt başka bir işlem tarafından değiştirildi. Tekrar deneyin.", ex);
        }
        return new PlacedOrder(getOrder(orderId, userId), false);
    }

    private Long placeOrder(Long userId, CreateOrderDto request, String idempotencyKey) {
        Map<Long, Integer> wanted = mergeItems(request.getItems());
        if (wanted.isEmpty()) {
            throw ApiException.badRequest("EMPTY_ORDER", "Sepette ürün yok.");
        }
        Address address = addressRepository.findByIdAndUserIdAndIsActiveTrue(request.getShippingAddressId(), userId)
                .orElseThrow(() -> ApiException.badRequest("INVALID_ADDRESS",
                        "Teslimat adresi geçersiz veya size ait değil."));
        PaymentMethod payment = paymentMethodRepository
                .findByIdAndUserIdAndIsActiveTrue(request.getPaymentMethodId(), userId)
                .orElseThrow(() -> ApiException.badRequest("INVALID_PAYMENT",
                        "Ödeme yöntemi geçersiz veya size ait değil."));
        ensureMatchesCart(userId, wanted);

        Order order = new Order();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> line : wanted.entrySet()) {
            Product product = productRepository.findById(line.getKey())
                    .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                    .orElseThrow(() -> checkoutFailed("Bir ürün artık satışta değil veya bulunamadı."));
            int quantity = line.getValue();
            if (product.getUnitInStock() == null || product.getUnitInStock() < quantity) {
                throw checkoutFailed("Yetersiz stok: " + product.getProductName()
                        + ". Miktarı azaltın veya sepetten çıkarın.");
            }
            BigDecimal unitPrice = CheckoutPricing.salePrice(product.getUnitPrice(), product.getDiscount());
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(CheckoutPricing.lineTotal(unitPrice, quantity));
            order.addItem(item);
            subtotal = subtotal.add(item.getTotalPrice());
            product.setUnitInStock(product.getUnitInStock() - quantity);
        }

        CheckoutPricing.Quote quote = pricing.quote(subtotal, false);
        simulatePayment(payment);
        productRepository.flush(); // iyimser kilit çakışması burada (sipariş yazılmadan) yakalanır

        order.setOrderNumber(newOrderNumber());
        order.setUserId(userId);
        order.setSubtotalAmount(quote.subtotal());
        order.setShippingFee(quote.shippingFee());
        order.setTotalAmount(quote.grandTotal());
        order.setStatus(PENDING);
        order.setShippingAddressId(request.getShippingAddressId());
        order.setBillingAddressId(request.getShippingAddressId());
        order.setPaymentMethodId(payment.getId());
        order.setNotes(request.getNotes());
        // Salt okunur ilişkiler de bağlanır: open-session-in-view açıkken commit sonrası okuma bu
        // örneği önbellekten döndürür ve ilişkiler aksi halde boş kalırdı.
        order.setUser(userRepository.getReferenceById(userId));
        order.setShippingAddress(address);
        order.setBillingAddress(address);
        order.setPaymentMethod(payment);
        Order saved = orderRepository.saveAndFlush(order);

        if (idempotencyKey != null) {
            try {
                orderIdempotencyRepository.saveAndFlush(new OrderIdempotencyRecord(userId, idempotencyKey, saved.getId()));
            } catch (DataIntegrityViolationException ex) {
                throw new IdempotencyConflictException(ex);
            }
        }

        cartService.clearCart(userId);
        userNotifier.notify(userId, "Sipariş alındı", "#" + saved.getOrderNumber()
                + " siparişiniz oluşturuldu. Tutar: " + saved.getTotalAmount().toPlainString(),
                UserNotifier.TYPE_ORDER, "/orders/" + saved.getId());
        productCacheInvalidator.evictAfterCommit();
        OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), userId, saved.getOrderNumber(),
                saved.getTotalAmount(), System.currentTimeMillis());
        orderEventPublisher.ifAvailable(publisher -> AfterCommit.run(() -> publisher.publishOrderCreated(event)));
        return saved.getId();
    }

    /** Kullanıcı iptali: yalnızca {@code Pending}/{@code Processing}; stok iade edilir. */
    @Transactional
    public String cancelOrder(Long orderId, Long userId, String reason) {
        Order order = ownedOrder(orderId, userId);
        if (!PENDING.equals(order.getStatus()) && !PROCESSING.equals(order.getStatus())) {
            throw ApiException.badRequest("CANCEL_NOT_ALLOWED", "Bu sipariş iptal edilemez.");
        }
        if (reason != null && !reason.isBlank()) {
            order.setCancelReason(reason.trim());
        }
        changeStatus(order, CANCELLED);
        return "Sipariş iptal edildi";
    }

    /** Yalnızca teslim edilmiş siparişler için. */
    @Transactional
    public OrderDto requestReturn(Long orderId, Long userId, String reason) {
        Order order = ownedOrder(orderId, userId);
        if (!DELIVERED.equals(order.getStatus())) {
            throw ApiException.badRequest("RETURN_NOT_ALLOWED",
                    "Yalnızca teslim edilmiş siparişler için iade talebi oluşturulabilir.");
        }
        order.setReturnReason(reason.trim());
        order.setReturnRequestedAt(LocalDateTime.now());
        changeStatus(order, RETURN_REQUESTED);
        return toDto(order);
    }

    /** §5.5 demo lojistik: {@code Pending → Processing → Shipped → Delivered}. */
    @Transactional
    public OrderDto advanceDemoFulfillment(Long orderId, Long userId) {
        if (!ecommerceProperties.isDemoFulfillmentEnabled()) {
            throw ApiException.notFound("DEMO_FULFILLMENT_DISABLED",
                    "Sipariş lojistik simülasyonu bu sunucuda kapalı.");
        }
        Order order = ownedOrder(orderId, userId);
        String next = switch (order.getStatus()) {
            case PENDING -> PROCESSING;
            case PROCESSING -> SHIPPED;
            case SHIPPED -> DELIVERED;
            default -> throw ApiException.badRequest("DEMO_ADVANCE_INVALID_STATE",
                    "Bu sipariş durumu için simüle edilecek sonraki adım yok.");
        };
        changeStatus(order, next);
        return toDto(order);
    }

    /** §5.4 yönetici durum güncellemesi; değer harf duyarsız eşleşir ve kanonik yazımla saklanır. */
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusDto request) {
        String status = STATUSES.stream()
                .filter(s -> s.equalsIgnoreCase(request.getStatus().trim()))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("INVALID_STATUS",
                        "Geçersiz durum. Kullanın: " + String.join(", ", STATUSES) + "."));
        Order order = orderRepository.findDetailedById(orderId).orElseThrow(OrderService::orderNotFound);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            order.setNotes(request.getNotes().trim());
        }
        changeStatus(order, status);
        return toDto(order);
    }

    /**
     * Ortak durum geçişi: stok iadesi ({@code Pending/Processing → Cancelled} ve {@code → Returned}),
     * kargo/teslim alanları ve kullanıcı bildirimi. Aynı duruma geçiş hiçbir şey yapmaz.
     */
    private void changeStatus(Order order, String next) {
        String previous = order.getStatus();
        if (next.equals(previous)) {
            return;
        }
        boolean restock = RETURNED.equals(next)
                || (CANCELLED.equals(next) && (PENDING.equals(previous) || PROCESSING.equals(previous)));
        if (restock) {
            restoreStock(order);
        }
        LocalDateTime now = LocalDateTime.now();
        if (SHIPPED.equals(next)) {
            order.setShippedAt(now);
            if (order.getCarrier() == null || order.getCarrier().isBlank()) {
                order.setCarrier(DEFAULT_CARRIER);
            }
            if (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank()) {
                order.setTrackingNumber("TR" + String.format("%010d",
                        ThreadLocalRandom.current().nextLong(10_000_000_000L)));
            }
            order.setEstimatedDeliveryAt(now.plusDays(ESTIMATED_DELIVERY_DAYS));
        } else if (DELIVERED.equals(next)) {
            order.setDeliveredAt(now);
        }
        order.setStatus(next);
        orderRepository.saveAndFlush(order);
        notifyStatusChange(order);
    }

    private void notifyStatusChange(Order order) {
        String[] text = switch (order.getStatus()) {
            case PROCESSING -> new String[] { "Sipariş hazırlanıyor", "siparişiniz hazırlanıyor." };
            case SHIPPED -> new String[] { "Sipariş kargoya verildi", "siparişiniz kargoya verildi." };
            case DELIVERED -> new String[] { "Sipariş teslim edildi", "siparişiniz teslim edildi." };
            case CANCELLED -> new String[] { "Sipariş iptal edildi", "siparişiniz iptal edildi." };
            case RETURN_REQUESTED -> new String[] { "İade talebi alındı", "siparişiniz için iade talebi alındı." };
            case RETURNED -> new String[] { "İade tamamlandı", "siparişiniz için iade tamamlandı." };
            default -> null;
        };
        if (text != null) {
            userNotifier.notify(order.getUserId(), text[0], "#" + order.getOrderNumber() + " " + text[1],
                    UserNotifier.TYPE_ORDER, "/orders/" + order.getId());
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId())
                    .ifPresent(p -> p.setUnitInStock(p.getUnitInStock() + item.getQuantity()));
        }
        productCacheInvalidator.evictAfterCommit();
    }

    /** Sunucu sepeti doluysa istenen kalemler sepetle birebir (ürün + adet) aynı olmalıdır (§5.2 adım 5). */
    private void ensureMatchesCart(Long userId, Map<Long, Integer> wanted) {
        CartDto cart = cartService.getCart(userId);
        if (cart.items().isEmpty()) {
            return;
        }
        if (cart.items().stream().anyMatch(i -> !Boolean.TRUE.equals(i.isAvailable()))) {
            throw ApiException.badRequest("CART_UNAVAILABLE",
                    "Sepetinizdeki bazı ürünler artık uygun değil. Sepetinizi güncelleyin.");
        }
        Map<Long, Integer> inCart = cart.items().stream()
                .collect(Collectors.toMap(CartItemDto::productId, CartItemDto::quantity, Integer::sum));
        if (!inCart.equals(wanted)) {
            throw ApiException.badRequest("CART_MISMATCH",
                    "Sepet ile ödeme özeti uyuşmuyor. Sayfayı yenileyip tekrar deneyin.");
        }
    }

    private void simulatePayment(PaymentMethod payment) {
        if (payment.getExpiryMonth() != null && payment.getExpiryYear() != null
                && PaymentCards.isExpired(payment.getExpiryMonth(), payment.getExpiryYear(), LocalDate.now())) {
            throw checkoutFailed("Kartın son kullanma tarihi geçmiş. Lütfen başka bir kart seçin.");
        }
        int declinePercent = checkoutProperties.getSimulatedPaymentDeclinePercent();
        if (declinePercent > 0 && ThreadLocalRandom.current().nextInt(100) < declinePercent) {
            throw checkoutFailed("Ödeme bankanız tarafından onaylanmadı. Lütfen başka bir kart deneyin.");
        }
    }

    private static Map<Long, Integer> mergeItems(List<CreateOrderItemDto> items) {
        Map<Long, Integer> merged = new LinkedHashMap<>();
        if (items != null) {
            for (CreateOrderItemDto item : items) {
                if (item != null && item.getProductId() != null && item.getQuantity() != null) {
                    merged.merge(item.getProductId(), item.getQuantity(), Integer::sum);
                }
            }
        }
        return merged;
    }

    private Order ownedOrder(Long orderId, Long userId) {
        return orderRepository.findDetailedById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(OrderService::orderNotFound);
    }

    private static ApiException orderNotFound() {
        return ApiException.notFound("ORDER_NOT_FOUND", "Sipariş bulunamadı");
    }

    private static ApiException checkoutFailed(String message) {
        return ApiException.badRequest("CHECKOUT_FAILED", message);
    }

    /** {@code ORD-yyyyMMdd-XXXXXXXX} (8 büyük harf hex). */
    private static String newOrderNumber() {
        return "ORD-" + LocalDate.now().format(ORDER_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        User user = order.getUser();
        if (user != null) {
            dto.setUserName((user.getFirstName() + " " + user.getLastName()).trim());
            dto.setUserEmail(user.getEmail());
        }
        dto.setItems(order.getItems().stream().map(OrderService::toItemDto).toList());
        dto.setSubtotalAmount(order.getSubtotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setNotes(order.getNotes());
        dto.setShippingAddress(order.getShippingAddress() == null ? null : AddressDto.from(order.getShippingAddress()));
        dto.setBillingAddress(order.getBillingAddress() == null ? null : AddressDto.from(order.getBillingAddress()));
        dto.setPaymentMethod(order.getPaymentMethod() == null ? null : PaymentMethodDto.from(order.getPaymentMethod()));
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setCarrier(order.getCarrier());
        dto.setShippedAt(order.getShippedAt());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setEstimatedDeliveryAt(order.getEstimatedDeliveryAt());
        dto.setCancelReason(order.getCancelReason());
        dto.setReturnReason(order.getReturnReason());
        dto.setReturnRequestedAt(order.getReturnRequestedAt());
        boolean advanceable = PENDING.equals(order.getStatus()) || PROCESSING.equals(order.getStatus())
                || SHIPPED.equals(order.getStatus());
        dto.setDemoNextAction(ecommerceProperties.isDemoFulfillmentEnabled() && advanceable ? DEMO_NEXT_ACTION : null);
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    private static OrderItemDto toItemDto(OrderItem item) {
        Product product = item.getProduct();
        return new OrderItemDto(
                item.getId(),
                item.getProductId(),
                product == null ? null : product.getProductName(),
                product == null ? null : product.getImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice());
    }
}
