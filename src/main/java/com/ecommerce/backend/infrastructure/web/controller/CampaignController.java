package com.ecommerce.backend.infrastructure.web.controller;

import com.ecommerce.backend.application.dto.*;
import com.ecommerce.backend.application.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaign")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<BaseResponseDto<java.util.List<CampaignDto>>> getAllCampaigns() {
        log.info("Getting all campaigns");
        BaseResponseDto<java.util.List<CampaignDto>> response = campaignService.getAllCampaigns();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<BaseResponseDto<java.util.List<CampaignDto>>> getActiveCampaigns() {
        log.info("Getting active campaigns");
        BaseResponseDto<java.util.List<CampaignDto>> response = campaignService.getActiveCampaigns();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDto<CampaignDto>> getCampaignById(@PathVariable Long id) {
        log.info("Getting campaign by id: {}", id);
        BaseResponseDto<CampaignDto> response = campaignService.getCampaignById(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<CampaignDto>> createCampaign(
            @Valid @RequestBody CreateCampaignDto createCampaignDto) {
        log.info("Creating new campaign: {}", createCampaignDto.getTitle());
        BaseResponseDto<CampaignDto> response = campaignService.createCampaign(createCampaignDto);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<CampaignDto>> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCampaignDto updateCampaignDto) {
        log.info("Updating campaign with id: {}", id);
        BaseResponseDto<CampaignDto> response = campaignService.updateCampaign(id, updateCampaignDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponseDto<String>> deleteCampaign(@PathVariable Long id) {
        log.info("Deleting campaign with id: {}", id);
        BaseResponseDto<String> response = campaignService.deleteCampaign(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
