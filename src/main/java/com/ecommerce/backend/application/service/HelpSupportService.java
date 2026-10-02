package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.ContactFormDto;
import com.ecommerce.backend.application.dto.CreateHelpArticleDto;
import com.ecommerce.backend.application.dto.CreateSupportTicketDto;
import com.ecommerce.backend.application.dto.FaqDto;
import com.ecommerce.backend.application.dto.HelpArticleDto;
import com.ecommerce.backend.application.dto.SupportMessageDto;
import com.ecommerce.backend.application.dto.SupportTicketDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Faq;
import com.ecommerce.backend.domain.entity.HelpArticle;
import com.ecommerce.backend.domain.entity.SupportMessage;
import com.ecommerce.backend.domain.entity.SupportTicket;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.FaqRepository;
import com.ecommerce.backend.infrastructure.repository.HelpArticleRepository;
import com.ecommerce.backend.infrastructure.repository.SupportMessageRepository;
import com.ecommerce.backend.infrastructure.repository.SupportTicketRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Yardım makaleleri, SSS, iletişim formu ve destek talepleri — docs/API_CONTRACT.md §4.15.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HelpSupportService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final HelpArticleRepository helpArticleRepository;
    private final FaqRepository faqRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserRepository userRepository;

    /** Yayımlanmış makaleler, en yeni önce; {@code pageNumber} 1 tabanlı. */
    public List<HelpArticleDto> getArticles(String category, int pageNumber, int pageSize) {
        Pageable page = PageRequest.of(pageNumber - 1, pageSize, NEWEST_FIRST);
        List<HelpArticle> articles = isBlank(category)
                ? helpArticleRepository.findByIsPublishedTrueAndIsActiveTrue(page)
                : helpArticleRepository.findByCategoryAndIsPublishedTrueAndIsActiveTrue(category.trim(), page);
        return articles.stream().map(HelpSupportService::toDto).toList();
    }

    /** Görüntülenme sayısını artırır; yoksa 404 {@code ARTICLE_NOT_FOUND}. */
    @Transactional
    public HelpArticleDto viewArticle(Long articleId) {
        HelpArticle article = helpArticleRepository.findByIdAndIsPublishedTrueAndIsActiveTrue(articleId)
                .orElseThrow(() -> ApiException.notFound("ARTICLE_NOT_FOUND", "Help article not found"));
        article.setViewCount(article.getViewCount() + 1);
        article.setUpdatedAt(LocalDateTime.now());
        return toDto(helpArticleRepository.save(article));
    }

    @Transactional
    public HelpArticleDto createArticle(CreateHelpArticleDto dto) {
        HelpArticle article = new HelpArticle();
        article.setTitle(dto.getTitle().trim());
        article.setContent(dto.getContent().trim());
        article.setCategory(dto.getCategory().trim());
        article.setTags(joinTags(dto.getTags()));
        article.setViewCount(0);
        article.setIsPublished(!Boolean.FALSE.equals(dto.getIsPublished()));
        article.setIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        article.setCreatedAt(now);
        article.setUpdatedAt(now);
        return toDto(helpArticleRepository.save(article));
    }

    public List<FaqDto> getFaqs(String category, int pageNumber, int pageSize) {
        Pageable page = PageRequest.of(pageNumber - 1, pageSize, NEWEST_FIRST);
        List<Faq> faqs = isBlank(category)
                ? faqRepository.findByIsPublishedTrueAndIsActiveTrue(page)
                : faqRepository.findByCategoryAndIsPublishedTrueAndIsActiveTrue(category.trim(), page);
        return faqs.stream().map(HelpSupportService::toDto).toList();
    }

    /** İletişim formu yalnızca loglanır. */
    public void submitContactForm(ContactFormDto dto) {
        log.info("İletişim formu alındı: category={}, subject={}, email={}",
                isBlank(dto.getCategory()) ? "General" : dto.getCategory().trim(), dto.getSubject(), dto.getEmail());
    }

    public List<SupportTicketDto> getUserTickets(Long userId, int pageNumber, int pageSize) {
        return withMessages(supportTicketRepository.findByUserIdAndIsActiveTrue(userId,
                PageRequest.of(pageNumber - 1, pageSize, NEWEST_FIRST)));
    }

    public List<SupportTicketDto> getAllTickets(int pageNumber, int pageSize) {
        return withMessages(supportTicketRepository.findByIsActiveTrue(
                PageRequest.of(pageNumber - 1, pageSize, NEWEST_FIRST)));
    }

    @Transactional
    public SupportTicketDto createTicket(Long userId, CreateSupportTicketDto dto) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> ApiException.badRequest("USER_NOT_FOUND", "User not found"));
        SupportTicket ticket = new SupportTicket();
        ticket.setUserId(userId);
        ticket.setSubject(dto.getSubject().trim());
        ticket.setDescription(dto.getDescription().trim());
        ticket.setCategory(dto.getCategory().trim());
        ticket.setPriority(isBlank(dto.getPriority()) ? SupportTicket.DEFAULT_PRIORITY : dto.getPriority().trim());
        ticket.setStatus(SupportTicket.STATUS_OPEN);
        ticket.setIsActive(true);
        LocalDateTime now = LocalDateTime.now();
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        SupportTicket saved = supportTicketRepository.save(ticket);
        log.info("Destek talebi oluşturuldu: id={}, userId={}", saved.getId(), userId);
        return toDto(saved, fullName(user), List.of());
    }

    private List<SupportTicketDto> withMessages(List<SupportTicket> tickets) {
        if (tickets.isEmpty()) {
            return List.of();
        }
        Map<Long, List<SupportMessageDto>> messages = supportMessageRepository
                .findByTicketIdInAndIsActiveTrueOrderByCreatedAtAscIdAsc(
                        tickets.stream().map(SupportTicket::getId).toList())
                .stream()
                .map(HelpSupportService::toDto)
                .collect(Collectors.groupingBy(SupportMessageDto::ticketId));
        return tickets.stream()
                .map(t -> toDto(t, fullName(t.getUser()), messages.getOrDefault(t.getId(), List.of())))
                .toList();
    }

    private static String joinTags(List<String> tags) {
        if (tags == null) {
            return "";
        }
        return tags.stream().filter(Objects::nonNull).map(String::trim).filter(t -> !t.isEmpty())
                .collect(Collectors.joining(","));
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(",")).map(String::trim).filter(t -> !t.isEmpty()).toList();
    }

    private static String fullName(User user) {
        return user == null ? "" : (user.getFirstName() + " " + user.getLastName()).trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static HelpArticleDto toDto(HelpArticle a) {
        return new HelpArticleDto(a.getId(), a.getTitle(), a.getContent(), a.getCategory(), splitTags(a.getTags()),
                a.getViewCount(), a.getIsPublished(), a.getCreatedAt(), a.getUpdatedAt());
    }

    private static FaqDto toDto(Faq f) {
        return new FaqDto(f.getId(), f.getQuestion(), f.getAnswer(), f.getCategory(), f.getViewCount(),
                f.getIsPublished(), f.getCreatedAt(), f.getUpdatedAt());
    }

    private static SupportTicketDto toDto(SupportTicket t, String userName, List<SupportMessageDto> messages) {
        return new SupportTicketDto(t.getId(), t.getUserId(), userName, t.getSubject(), t.getDescription(),
                t.getCategory(), t.getPriority(), t.getStatus(), t.getAssignedTo(), t.getCreatedAt(),
                t.getUpdatedAt(), messages);
    }

    private static SupportMessageDto toDto(SupportMessage m) {
        return new SupportMessageDto(m.getId(), m.getTicketId(), m.getUserId(), fullName(m.getUser()),
                m.getMessage(), m.getIsFromSupport(), m.getCreatedAt());
    }
}
