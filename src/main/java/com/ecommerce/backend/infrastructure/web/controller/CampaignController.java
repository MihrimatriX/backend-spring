package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.BaseResponseDto;
import com.ecommerce.backend.application.dto.CampaignDto;
import com.ecommerce.backend.application.dto.CampaignRequestDto;
import com.ecommerce.backend.application.service.CampaignService;
import com.ecommerce.backend.infrastructure.web.support.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/campaign")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Kampanyalar (§4.5)")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<List<CampaignDto>>> getAll() {
        return ApiResponses.ok("Campaigns retrieved successfully", campaignService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<BaseResponseDto<List<CampaignDto>>> getActive() {
        return ApiResponses.ok("Active campaigns retrieved successfully", campaignService.getRunning());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<CampaignDto>> getById(@PathVariable Long id) {
        return ApiResponses.ok("Campaign retrieved successfully", campaignService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<CampaignDto>> create(@Valid @RequestBody CampaignRequestDto request) {
        return ApiResponses.created("Campaign created successfully", campaignService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<CampaignDto>> update(@PathVariable Long id,
            @Valid @RequestBody CampaignRequestDto request) {
        return ApiResponses.ok("Campaign updated successfully", campaignService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<String>> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ApiResponses.ok("Campaign deleted successfully", "Campaign deleted successfully");
    }
}
