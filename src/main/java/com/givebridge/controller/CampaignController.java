package com.givebridge.controller;

import com.givebridge.dto.CampaignRequestDTO;
import com.givebridge.model.Campaign;
import com.givebridge.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Campaign endpoints.
 * Handles all incoming HTTP requests related to campaigns.
 * Delegates business logic to CampaignService.
 */
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * GET /api/campaigns
     * Retrieves all campaigns ordered by newest first.
     *
     * @return list of all campaigns with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        List<Campaign> campaigns = campaignService.getAllCampaigns();
        return ResponseEntity.ok(campaigns);
    }

    /**
     * GET /api/campaigns/{id}
     * Retrieves a single campaign by its ID.
     *
     * @param id the campaign ID from the URL path
     * @return the campaign with HTTP 200 OK, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getCampaignById(@PathVariable Long id) {
        Campaign campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(campaign);
    }

    /**
     * POST /api/campaigns
     * Creates a new campaign.
     *
     * @param dto the campaign data from the request body
     * @return the created campaign with HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<Campaign> createCampaign(@Valid @RequestBody CampaignRequestDTO dto) {
        Campaign created = campaignService.createCampaign(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/campaigns/{id}
     * Updates an existing campaign.
     *
     * @param id  the campaign ID from the URL path
     * @param dto the updated campaign data from the request body
     * @return the updated campaign with HTTP 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Campaign> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequestDTO dto) {
        Campaign updated = campaignService.updateCampaign(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/campaigns/{id}
     * Deletes a campaign by its ID.
     *
     * @param id the campaign ID from the URL path
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}