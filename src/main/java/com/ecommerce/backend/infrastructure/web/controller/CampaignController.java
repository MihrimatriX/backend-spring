package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CampaignDto;
import com.ecommerce.backend.application.dto.CampaignRequestDto;
import com.ecommerce.backend.application.service.CampaignService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Kampanyalar — docs/API_CONTRACT.md §4.5 (yazma uçları Admin, SecurityConfig'te). */
@RestController
@RequestMapping("/api/campaign")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Campaign management operations")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    @Operation(summary = "Get all campaigns", description = "Active campaigns, newest first")
    public ResponseEntity<BaseResponseDto<List<CampaignDto>>> getAllCampaigns() {
        return ApiResponses.ok("Campaigns retrieved successfully", campaignService.getAllCampaigns());
    }

    @GetMapping("/active")
    @Operation(summary = "Get running campaigns", description = "Active and startDate <= now <= endDate")
    public ResponseEntity<BaseResponseDto<List<CampaignDto>>> getActiveCampaigns() {
        return ApiResponses.ok("Active campaigns retrieved successfully", campaignService.getActiveCampaigns());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign by ID")
    public ResponseEntity<BaseResponseDto<CampaignDto>> getCampaign(@PathVariable Long id) {
        return ApiResponses.ok("Campaign retrieved successfully", campaignService.getCampaign(id));
    }

    @PostMapping
    @Operation(summary = "Create campaign")
    public ResponseEntity<BaseResponseDto<CampaignDto>> createCampaign(@Valid @RequestBody CampaignRequestDto dto) {
        return ApiResponses.created("Campaign created successfully", campaignService.createCampaign(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update campaign")
    public ResponseEntity<BaseResponseDto<CampaignDto>> updateCampaign(@PathVariable Long id,
            @Valid @RequestBody CampaignRequestDto dto) {
        return ApiResponses.ok("Campaign updated successfully", campaignService.updateCampaign(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete campaign (soft)")
    public ResponseEntity<BaseResponseDto<String>> deleteCampaign(@PathVariable Long id) {
        String message = campaignService.deleteCampaign(id);
        return ApiResponses.ok(message, message);
    }
}
