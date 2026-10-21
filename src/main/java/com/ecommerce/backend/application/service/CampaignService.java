package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.CampaignDto;
import com.ecommerce.backend.application.dto.CampaignRequestDto;
import com.ecommerce.backend.application.exception.ApiException;
import com.ecommerce.backend.domain.entity.Campaign;
import com.ecommerce.backend.infrastructure.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/** Kampanyalar (docs/API_CONTRACT.md §4.5). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository campaignRepository;

    /** Aktif kampanyalar, en yeni önce. */
    public List<CampaignDto> getAllCampaigns() {
        return campaignRepository.findByIsActiveTrueOrderByCreatedAtDescIdDesc().stream()
                .map(CampaignService::toDto)
                .toList();
    }

    /** Aktif ve {@code startDate ≤ şimdi(UTC) ≤ endDate}. */
    public List<CampaignDto> getActiveCampaigns() {
        return campaignRepository.findRunning(LocalDateTime.now(ZoneOffset.UTC)).stream()
                .map(CampaignService::toDto)
                .toList();
    }

    public CampaignDto getCampaign(Long id) {
        return campaignRepository.findByIdAndIsActiveTrue(id)
                .map(CampaignService::toDto)
                .orElseThrow(CampaignService::campaignNotFound);
    }

    @Transactional
    public CampaignDto createCampaign(CampaignRequestDto request) {
        Campaign campaign = new Campaign();
        apply(campaign, request);
        campaign.setIsActive(request.isActive() == null || request.isActive());
        return toDto(campaignRepository.saveAndFlush(campaign));
    }

    /** Pasif kampanyalar da güncellenebilir; {@code isActive} gönderilmezse değişmez. */
    @Transactional
    public CampaignDto updateCampaign(Long id, CampaignRequestDto request) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow(CampaignService::campaignNotFound);
        apply(campaign, request);
        if (request.isActive() != null) {
            campaign.setIsActive(request.isActive());
        }
        return toDto(campaignRepository.saveAndFlush(campaign));
    }

    /** Yumuşak silme. */
    @Transactional
    public String deleteCampaign(Long id) {
        Campaign campaign = campaignRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(CampaignService::campaignNotFound);
        campaign.setIsActive(false);
        campaignRepository.save(campaign);
        return "Campaign deleted successfully";
    }

    private static void apply(Campaign campaign, CampaignRequestDto request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw ApiException.badRequest("INVALID_DATE_RANGE", "End date must be after start date");
        }
        campaign.setTitle(request.title().trim());
        campaign.setSubtitle(request.subtitle());
        campaign.setDescription(request.description());
        campaign.setDiscount(request.discount() == null ? 0 : request.discount());
        campaign.setImageUrl(request.imageUrl());
        campaign.setBackgroundColor(request.backgroundColor());
        campaign.setTimeLeft(request.timeLeft());
        campaign.setButtonText(request.buttonText());
        campaign.setButtonHref(request.buttonHref());
        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());
    }

    private static ApiException campaignNotFound() {
        return ApiException.notFound("CAMPAIGN_NOT_FOUND", "Campaign not found");
    }

    static CampaignDto toDto(Campaign campaign) {
        return new CampaignDto(
                campaign.getId(),
                campaign.getTitle(),
                campaign.getSubtitle(),
                campaign.getDescription(),
                campaign.getDiscount(),
                campaign.getImageUrl(),
                campaign.getBackgroundColor(),
                campaign.getTimeLeft(),
                campaign.getButtonText(),
                campaign.getButtonHref(),
                campaign.getIsActive(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                campaign.getCreatedAt(),
                campaign.getUpdatedAt());
    }
}
