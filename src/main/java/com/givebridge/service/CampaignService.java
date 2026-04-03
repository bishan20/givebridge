package com.givebridge.service;

import com.givebridge.dto.CampaignRequestDTO;
import com.givebridge.model.Campaign;
import com.givebridge.repository.CampaignRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for Campaign business logic.
 * Sits between the controller and repository.
 * Handles all business rules and data transformation.
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;

    /**
     * Retrieves all campaigns ordered by creation date, newest first.
     *
     * @return list of all campaigns
     */
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieves a single campaign by its ID.
     * Throws EntityNotFoundException if the campaign does not exist.
     *
     * @param id the campaign ID
     * @return the found campaign
     */
    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Campaign not found with id: " + id));
    }

    /**
     * Creates a new campaign from the provided request data.
     * Maps DTO fields to a Campaign entity and saves to the database.
     *
     * @param dto the campaign request data
     * @return the saved campaign
     */
    public Campaign createCampaign(CampaignRequestDTO dto) {
        Campaign campaign = Campaign.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .goalAmount(dto.getGoalAmount())
                .deadline(dto.getDeadline())
                .build();
        return campaignRepository.save(campaign);
    }

    /**
     * Updates an existing campaign with new data.
     * Throws EntityNotFoundException if the campaign does not exist.
     *
     * @param id  the ID of the campaign to update
     * @param dto the updated campaign data
     * @return the updated campaign
     */
    public Campaign updateCampaign(Long id, CampaignRequestDTO dto) {
        Campaign existing = getCampaignById(id);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setGoalAmount(dto.getGoalAmount());
        existing.setDeadline(dto.getDeadline());
        return campaignRepository.save(existing);
    }

    /**
     * Deletes a campaign by its ID.
     * Throws EntityNotFoundException if the campaign does not exist.
     *
     * @param id the ID of the campaign to delete
     */
    public void deleteCampaign(Long id) {
        Campaign existing = getCampaignById(id);
        campaignRepository.delete(existing);
    }
}