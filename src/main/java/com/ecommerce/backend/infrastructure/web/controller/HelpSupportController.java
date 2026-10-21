package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.ContactFormDto;
import com.ecommerce.backend.application.dto.CreateHelpArticleDto;
import com.ecommerce.backend.application.dto.CreateSupportTicketDto;
import com.ecommerce.backend.application.dto.FaqDto;
import com.ecommerce.backend.application.dto.HelpArticleDto;
import com.ecommerce.backend.application.dto.SupportTicketDto;
import com.ecommerce.backend.application.service.HelpSupportService;
import com.ecommerce.backend.infrastructure.security.CurrentUserService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * docs/API_CONTRACT.md §4.15 — makale/SSS/iletişim anonim, talepler oturum ister, yönetim uçları Admin.
 */
@RestController
@RequestMapping("/api/helpsupport")
@RequiredArgsConstructor
@Tag(name = "Help & Support", description = "Yardım makaleleri, SSS, iletişim ve destek talepleri")
public class HelpSupportController {

    private static final String CONTACT_REPLY = "Thank you for your message. We will get back to you soon.";

    private final HelpSupportService helpSupportService;
    private final CurrentUserService currentUserService;

    @GetMapping("/articles")
    @Operation(summary = "Yayımlanmış yardım makaleleri (en yeni önce)")
    public ResponseEntity<BaseResponseDto<List<HelpArticleDto>>> getArticles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Help articles retrieved successfully", helpSupportService.getArticles(category,
                ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 10)));
    }

    @GetMapping("/articles/{articleId}")
    @Operation(summary = "Makale ayrıntısı (görüntülenme sayısı artar); yoksa 404 ARTICLE_NOT_FOUND")
    public ResponseEntity<BaseResponseDto<HelpArticleDto>> getArticle(@PathVariable Long articleId) {
        return ApiResponses.ok("Help article retrieved successfully", helpSupportService.viewArticle(articleId));
    }

    @PostMapping("/articles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Yönetici: makale oluştur (201)")
    public ResponseEntity<BaseResponseDto<HelpArticleDto>> createArticle(
            @Valid @RequestBody CreateHelpArticleDto request) {
        return ApiResponses.created("Help article created successfully", helpSupportService.createArticle(request));
    }

    @GetMapping("/faqs")
    @Operation(summary = "Yayımlanmış SSS kayıtları")
    public ResponseEntity<BaseResponseDto<List<FaqDto>>> getFaqs(@RequestParam(required = false) String category,
            @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("FAQs retrieved successfully", helpSupportService.getFaqs(category,
                ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 10)));
    }

    @PostMapping("/contact")
    @Operation(summary = "İletişim formu (anonim)")
    public ResponseEntity<BaseResponseDto<String>> submitContactForm(@Valid @RequestBody ContactFormDto request) {
        helpSupportService.submitContactForm(request);
        return ApiResponses.ok("Contact form submitted successfully", CONTACT_REPLY);
    }

    @GetMapping("/tickets")
    @Operation(summary = "Çağıranın destek talepleri")
    public ResponseEntity<BaseResponseDto<List<SupportTicketDto>>> getUserTickets(
            @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Support tickets retrieved successfully", helpSupportService.getUserTickets(
                currentUserService.requireUserId(), ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 10)));
    }

    @PostMapping("/tickets")
    @Operation(summary = "Destek talebi oluştur (201, status Open)")
    public ResponseEntity<BaseResponseDto<SupportTicketDto>> createTicket(
            @Valid @RequestBody CreateSupportTicketDto request) {
        return ApiResponses.created("Support ticket created successfully",
                helpSupportService.createTicket(currentUserService.requireUserId(), request));
    }

    @GetMapping("/tickets/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Yönetici: tüm destek talepleri")
    public ResponseEntity<BaseResponseDto<List<SupportTicketDto>>> getAllTickets(
            @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize) {
        return ApiResponses.ok("Support tickets retrieved successfully", helpSupportService.getAllTickets(
                ApiResponses.page(pageNumber), ApiResponses.size(pageSize, 20)));
    }
}
