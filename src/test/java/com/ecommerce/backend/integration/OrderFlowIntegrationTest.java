package com.ecommerce.backend.integration;

import com.ecommerce.backend.application.dto.CreateOrderDto;
import com.ecommerce.backend.application.dto.CreateOrderItemDto;
import com.ecommerce.backend.application.service.OrderService;
import com.ecommerce.backend.domain.entity.Address;
import com.ecommerce.backend.domain.entity.Category;
import com.ecommerce.backend.domain.entity.PaymentMethod;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.exception.ConflictException;
import com.ecommerce.backend.infrastructure.repository.AddressRepository;
import com.ecommerce.backend.infrastructure.repository.CategoryRepository;
import com.ecommerce.backend.infrastructure.repository.OrderIdempotencyRepository;
import com.ecommerce.backend.infrastructure.repository.OrderRepository;
import com.ecommerce.backend.infrastructure.repository.PaymentMethodRepository;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.ShoppingCartRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderFlowIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderIdempotencyRepository orderIdempotencyRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @BeforeEach
    void clean() {
        orderIdempotencyRepository.deleteAll();
        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createOrderReducesStock() {
        CatalogFixture f = seedCatalog(5);

        var response = orderService.createOrder(f.userId(), buildOrderDto(f, 1), null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getOrderNumber()).startsWith("ORD-");

        Product updated = productRepository.findById(f.productId()).orElseThrow();
        assertThat(updated.getUnitInStock()).isEqualTo(4);
    }

    @Test
    void idempotencyKeyReturnsSameOrderWithoutDoubleStockDeduction() {
        CatalogFixture f = seedCatalog(5);
        String key = "idem-stable-key-001";

        var first = orderService.createOrder(f.userId(), buildOrderDto(f, 1), key);
        var second = orderService.createOrder(f.userId(), buildOrderDto(f, 1), key);

        assertThat(first.isSuccess()).isTrue();
        assertThat(second.isSuccess()).isTrue();
        assertThat(second.getData().getId()).isEqualTo(first.getData().getId());
        assertThat(second.getMessage()).containsIgnoringCase("Idempotent");

        Product updated = productRepository.findById(f.productId()).orElseThrow();
        assertThat(updated.getUnitInStock()).isEqualTo(4);
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void concurrentOrdersOnSameInventory_oneWinsOneFails() throws Exception {
        CatalogFixture f = seedCatalog(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        AtomicInteger businessErrors = new AtomicInteger();

        Future<?> a = pool.submit(() -> runConcurrentOrder(f, start, successes, conflicts, businessErrors, 0));
        Future<?> b = pool.submit(() -> runConcurrentOrder(f, start, successes, conflicts, businessErrors, 1));
        start.countDown();
        a.get();
        b.get();
        pool.shutdown();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get() + businessErrors.get()).isEqualTo(1);
        Product updated = productRepository.findById(f.productId()).orElseThrow();
        assertThat(updated.getUnitInStock()).isZero();
    }

    private void runConcurrentOrder(
            CatalogFixture f,
            CountDownLatch start,
            AtomicInteger successes,
            AtomicInteger conflicts,
            AtomicInteger businessErrors,
            int idx) {
        try {
            start.await();
            var r = orderService.createOrder(f.userId(), buildOrderDto(f, 1), "parallel-idem-" + idx + "-xxxxxxxx");
            if (r.isSuccess()) {
                successes.incrementAndGet();
            } else {
                businessErrors.incrementAndGet();
            }
        } catch (ConflictException e) {
            conflicts.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private CreateOrderDto buildOrderDto(CatalogFixture f, int qty) {
        CreateOrderItemDto line = new CreateOrderItemDto();
        line.setProductId(f.productId());
        line.setQuantity(qty);
        CreateOrderDto dto = new CreateOrderDto();
        dto.setShippingAddressId(f.addressId());
        dto.setPaymentMethodId(f.paymentId());
        dto.setItems(List.of(line));
        return dto;
    }

    private CatalogFixture seedCatalog(int stock) {
        Category cat = new Category();
        cat.setCategoryName("Test Cat " + System.nanoTime());
        cat.setDescription("d");
        cat = categoryRepository.saveAndFlush(cat);

        Product p = new Product();
        p.setProductName("Widget");
        p.setUnitPrice(new BigDecimal("9.99"));
        p.setUnitInStock(stock);
        p.setQuantityPerUnit("1");
        p.setCategory(cat);
        p.setDescription("x");
        p = productRepository.saveAndFlush(p);

        User u = new User();
        u.setEmail("u" + System.nanoTime() + "@test.com");
        u.setPassword("secret12");
        u.setFirstName("T");
        u.setLastName("U");
        u = userRepository.saveAndFlush(u);

        Address addr = new Address();
        addr.setUserId(u.getId());
        addr.setTitle("Home");
        addr.setFullAddress("1 Test St");
        addr.setCity("Istanbul");
        addr.setDistrict("Kadikoy");
        addr.setPostalCode("34000");
        addr = addressRepository.saveAndFlush(addr);

        PaymentMethod pm = new PaymentMethod();
        pm.setUserId(u.getId());
        pm.setType("CreditCard");
        pm.setCardHolderName("T U");
        pm.setCardNumber("4111111111111111");
        pm.setExpiryMonth(12);
        pm.setExpiryYear(2028);
        pm.setCvv("123");
        pm = paymentMethodRepository.saveAndFlush(pm);

        return new CatalogFixture(u.getId(), p.getId(), addr.getId(), pm.getId());
    }

    private record CatalogFixture(long userId, long productId, long addressId, long paymentId) {
    }
}
