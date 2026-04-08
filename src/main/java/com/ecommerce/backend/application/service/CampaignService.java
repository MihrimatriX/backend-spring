package com.ecommerce.backend.application.service;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.domain.entity.Campaign;
import com.ecommerce.backend.infrastructure.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;

    @Transactional(readOnly = true)
    public BaseResponseDto<List<CampaignDto>> getAllCampaigns() {
        try {
            List<Campaign> campaigns = campaignRepository.findByIsActiveTrueOrderByCreatedAtDesc();
            List<CampaignDto> campaignDtos = campaigns.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Campaigns retrieved successfully", campaignDtos);
        } catch (Exception e) {
            log.error("Error retrieving campaigns: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving campaigns: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<List<CampaignDto>> getActiveCampaigns() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Campaign> campaigns = campaignRepository
                    .findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByCreatedAtDesc(now,
                            now);
            List<CampaignDto> campaignDtos = campaigns.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return BaseResponseDto.success("Active campaigns retrieved successfully", campaignDtos);
        } catch (Exception e) {
            log.error("Error retrieving active campaigns: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving active campaigns: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BaseResponseDto<CampaignDto> getCampaignById(Long id) {
        try {
            Campaign campaign = campaignRepository.findByIdAndIsActiveTrue(id)
                    .orElse(null);

            if (campaign == null) {
                return BaseResponseDto.error("Campaign not found");
            }

            CampaignDto campaignDto = convertToDto(campaign);
            return BaseResponseDto.success("Campaign retrieved successfully", campaignDto);
        } catch (Exception e) {
            log.error("Error retrieving campaign: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error retrieving campaign: " + e.getMessage());
        }
    }

    public BaseResponseDto<CampaignDto> createCampaign(CreateCampaignDto createCampaignDto) {
        try {
            Campaign campaign = new Campaign();
            campaign.setTitle(createCampaignDto.getTitle());
            campaign.setSubtitle(createCampaignDto.getSubtitle());
            campaign.setDescription(createCampaignDto.getDescription());
            campaign.setDiscount(createCampaignDto.getDiscount());
            campaign.setImageUrl(createCampaignDto.getImageUrl());
            campaign.setBackgroundColor(createCampaignDto.getBackgroundColor());
            campaign.setTimeLeft(createCampaignDto.getTimeLeft());
            campaign.setButtonText(createCampaignDto.getButtonText());
            campaign.setButtonHref(createCampaignDto.getButtonHref());
            campaign.setStartDate(createCampaignDto.getStartDate());
            campaign.setEndDate(createCampaignDto.getEndDate());
            campaign.setIsActive(createCampaignDto.getIsActive());
            campaign.setCreatedAt(LocalDateTime.now());
            campaign.setUpdatedAt(LocalDateTime.now());

            Campaign savedCampaign = campaignRepository.save(campaign);
            CampaignDto campaignDto = convertToDto(savedCampaign);

            return BaseResponseDto.success("Campaign created successfully", campaignDto);
        } catch (Exception e) {
            log.error("Error creating campaign: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error creating campaign: " + e.getMessage());
        }
    }

    public BaseResponseDto<CampaignDto> updateCampaign(Long id, UpdateCampaignDto updateCampaignDto) {
        try {
            Campaign campaign = campaignRepository.findByIdAndIsActiveTrue(id)
                    .orElse(null);

            if (campaign == null) {
                return BaseResponseDto.error("Campaign not found");
            }

            campaign.setTitle(updateCampaignDto.getTitle());
            campaign.setSubtitle(updateCampaignDto.getSubtitle());
            campaign.setDescription(updateCampaignDto.getDescription());
            campaign.setDiscount(updateCampaignDto.getDiscount());
            campaign.setImageUrl(updateCampaignDto.getImageUrl());
            campaign.setBackgroundColor(updateCampaignDto.getBackgroundColor());
            campaign.setTimeLeft(updateCampaignDto.getTimeLeft());
            campaign.setButtonText(updateCampaignDto.getButtonText());
            campaign.setButtonHref(updateCampaignDto.getButtonHref());
            campaign.setStartDate(updateCampaignDto.getStartDate());
            campaign.setEndDate(updateCampaignDto.getEndDate());
            campaign.setIsActive(updateCampaignDto.getIsActive());
            campaign.setUpdatedAt(LocalDateTime.now());

            Campaign savedCampaign = campaignRepository.save(campaign);
            CampaignDto campaignDto = convertToDto(savedCampaign);

            return BaseResponseDto.success("Campaign updated successfully", campaignDto);
        } catch (Exception e) {
            log.error("Error updating campaign: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error updating campaign: " + e.getMessage());
        }
    }

    public BaseResponseDto<String> deleteCampaign(Long id) {
        try {
            Campaign campaign = campaignRepository.findByIdAndIsActiveTrue(id)
                    .orElse(null);

            if (campaign == null) {
                return BaseResponseDto.error("Campaign not found");
            }

            campaign.setIsActive(false);
            campaign.setUpdatedAt(LocalDateTime.now());
            campaignRepository.save(campaign);

            return BaseResponseDto.success("Campaign deleted successfully", "Campaign deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting campaign: {}", e.getMessage(), e);
            return BaseResponseDto.error("Error deleting campaign: " + e.getMessage());
        }
    }

    private CampaignDto convertToDto(Campaign campaign) {
        CampaignDto dto = new CampaignDto();
        dto.setId(campaign.getId());
        dto.setTitle(campaign.getTitle());
        dto.setSubtitle(campaign.getSubtitle());
        dto.setDescription(campaign.getDescription());
        dto.setDiscount(campaign.getDiscount());
        dto.setImageUrl(campaign.getImageUrl());
        dto.setBackgroundColor(campaign.getBackgroundColor());
        dto.setTimeLeft(campaign.getTimeLeft());
        dto.setButtonText(campaign.getButtonText());
        dto.setButtonHref(campaign.getButtonHref());
        dto.setIsActive(campaign.getIsActive());
        dto.setStartDate(campaign.getStartDate());
        dto.setEndDate(campaign.getEndDate());
        dto.setCreatedAt(campaign.getCreatedAt());
        dto.setUpdatedAt(campaign.getUpdatedAt());
        return dto;
    }
}