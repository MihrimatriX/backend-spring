package com.ecommerce.backend.infrastructure.data;

import com.ecommerce.backend.domain.entity.Faq;
import com.ecommerce.backend.domain.entity.HelpArticle;
import com.ecommerce.backend.infrastructure.repository.FaqRepository;
import com.ecommerce.backend.infrastructure.repository.HelpArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Yardım merkezi içeriğini (makaleler ve SSS) her profilde idempotent olarak ekler: ilgili tablo boşsa
 * doldurur, aksi halde dokunmaz. İçerik .NET demo seed'iyle aynıdır (DemoDataSeeder.SeedHelpAndFaqAsync).
 */
@Component
@Order(50)
@RequiredArgsConstructor
@Slf4j
public class HelpContentInitializer implements ApplicationRunner {

    private record ArticleSeed(String title, String content, String category, String tags, int viewCount) {
    }

    private record FaqSeed(String question, String answer, String category, int viewCount) {
    }

    private static final List<ArticleSeed> ARTICLES = List.of(
            new ArticleSeed("Yardım merkezi — hızlı başlangıç",
                    "Hesabınızdan siparişlerinizi takip edebilir, iade talebi oluşturabilir ve adreslerinizi yönetebilirsiniz.",
                    "Hesap", "demo,başlangıç,hesap", 42),
            new ArticleSeed("Kargo ve teslimat süreleri",
                    "Standart kargo 2–4 iş günü, hızlı kargo 1–2 iş günü içinde teslim edilir. Tatil günleri süreye dahil değildir.",
                    "Kargo", "kargo,teslimat", 128),
            new ArticleSeed("Ödeme yöntemleri",
                    "Kredi kartı, banka kartı ve havale ile ödeme kabul edilir. Taksit seçenekleri bankanıza göre değişir.",
                    "Ödeme", "ödeme,kart", 90));

    private static final List<FaqSeed> FAQS = List.of(
            new FaqSeed("Siparişimi nasıl iptal ederim?",
                    "Hesabım > Siparişlerim üzerinden iptal edilebilir siparişlerde 'İptal' düğmesini kullanın. Kargoya verilmiş siparişlerde iade süreci geçerlidir.",
                    "Sipariş", 15),
            new FaqSeed("Ücretsiz kargo var mı?",
                    "Belirli tutarın üzerindeki siparişlerde ücretsiz kargo kampanyalarımız olabilir; ödeme adımında güncel koşullar gösterilir.",
                    "Kargo", 33),
            new FaqSeed("İade süresi ne kadar?",
                    "Ürün tesliminden itibaren 14 gün içinde cayma hakkınızı kullanabilirsiniz (istisnalar ürün sayfasında belirtilir).",
                    "İade", 21));

    private final HelpArticleRepository helpArticleRepository;
    private final FaqRepository faqRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        if (helpArticleRepository.count() == 0) {
            helpArticleRepository.saveAll(ARTICLES.stream().map(a -> {
                HelpArticle e = new HelpArticle();
                e.setTitle(a.title());
                e.setContent(a.content());
                e.setCategory(a.category());
                e.setTags(a.tags());
                e.setViewCount(a.viewCount());
                e.setIsPublished(true);
                e.setIsActive(true);
                e.setCreatedAt(now);
                e.setUpdatedAt(now);
                return e;
            }).toList());
            log.info("{} yardım makalesi eklendi", ARTICLES.size());
        }
        if (faqRepository.count() == 0) {
            faqRepository.saveAll(FAQS.stream().map(f -> {
                Faq e = new Faq();
                e.setQuestion(f.question());
                e.setAnswer(f.answer());
                e.setCategory(f.category());
                e.setViewCount(f.viewCount());
                e.setIsPublished(true);
                e.setIsActive(true);
                e.setCreatedAt(now);
                e.setUpdatedAt(now);
                return e;
            }).toList());
            log.info("{} SSS kaydı eklendi", FAQS.size());
        }
    }
}
