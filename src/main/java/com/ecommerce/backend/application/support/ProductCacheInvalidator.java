package com.ecommerce.backend.application.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Sipariş/iptal stok değiştirdiğinde ürün önbelleğini ({@code products}) boşaltır; aksi halde
 * {@code GET /api/product/{id}} eski {@code unitInStock} değerini döndürür.
 */
@Component
@Slf4j
public class ProductCacheInvalidator {

    static final String PRODUCTS_CACHE = "products";

    private final ObjectProvider<CacheManager> cacheManager;

    public ProductCacheInvalidator(ObjectProvider<CacheManager> cacheManager) {
        this.cacheManager = cacheManager;
    }

    /** Transaction commit edildikten sonra (yoksa hemen) önbelleği temizler. */
    public void evictAfterCommit() {
        AfterCommit.run(this::evictNow);
    }

    private void evictNow() {
        CacheManager manager = cacheManager.getIfAvailable();
        Cache cache = manager != null ? manager.getCache(PRODUCTS_CACHE) : null;
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
        } catch (RuntimeException ex) {
            // Önbellek (Redis) erişilemiyorsa sipariş yine de tamamlanmıştır; TTL ile tazelenir.
            log.warn("Ürün önbelleği temizlenemedi: {}", ex.getMessage());
        }
    }
}
