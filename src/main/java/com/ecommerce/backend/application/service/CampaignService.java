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
import java.util.List;

/**
 * Kampanyalar — docs/API_CONTRACT.md §4.5.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository campaignRepository;

    public List<CampaignDto> getAll() {
        return campaignRepository.findByIsActiveTrueOrderByCreatedAtDescIdDesc().stream().map(CampaignService::toDto)
                .toList();
    }

    public List<CampaignDto> getRunning() {
        return campaignRepository.findRunning(LocalDateTime.now()).stream().map(CampaignService::toDto).toList();
    }

    public CampaignDto getById(Long id) {
        return campaignRepository.findByIdAndIsActiveTrue(id).map(CampaignService::toDto)
                .orElseThrow(CampaignService::notFound);
    }

    @Transactional
    public CampaignDto create(CampaignRequestDto request) {
        Campaign campaign = new Campaign();
        apply(campaign, request);
        campaign.setIsActive(request.isActive() == null || request.isActive());
        return toDto(campaignRepository.save(campaign));
    }

    @Transactional
    public CampaignDto update(Long id, CampaignRequestDto request) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow(CampaignService::notFound);
        apply(campaign, request);
        if (request.isActive() != null) {
            campaign.setIsActive(request.isActive());
        }
        return toDto(campaignRepository.save(campaign));
    }

    @Transactional
    public void delete(Long id) {
        Campaign campaign = campaignRepository.findByIdAndIsActiveTrue(id).orElseThrow(CampaignService::notFound);
        campaign.setIsActive(false);
        campaignRepository.save(campaign);
    }

    private static void apply(Campaign c, CampaignRequestDto r) {
        if (r.endDate().isBefore(r.startDate())) {
            throw ApiException.badRequest("INVALID_DATE_RANGE", "End date must be after start date");
        }
        c.setTitle(r.title().trim());
        c.setSubtitle(r.subtitle());
        c.setDescription(r.description());
        c.setDiscount(r.discount() == null ? 0 : r.discount());
        c.setImageUrl(r.imageUrl());
        c.setBackgroundColor(r.backgroundColor());
        c.setTimeLeft(r.timeLeft());
        c.setButtonText(r.buttonText());
        c.setButtonHref(r.buttonHref());
        c.setStartDate(r.startDate());
        c.setEndDate(r.endDate());
    }

    private static ApiException notFound() {
        return ApiException.notFound("CAMPAIGN_NOT_FOUND", "Campaign not found");
    }

    private static CampaignDto toDto(Campaign c) {
        return new CampaignDto(c.getId(), c.getTitle(), c.getSubtitle(), c.getDescription(), c.getDiscount(),
                c.getImageUrl(), c.getBackgroundColor(), c.getTimeLeft(), c.getButtonText(), c.getButtonHref(),
                c.getIsActive(), c.getStartDate(), c.getEndDate(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
