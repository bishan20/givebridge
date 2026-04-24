package com.givebridge.service;

import com.givebridge.dto.CampaignRequestDTO;
import com.givebridge.dto.CampaignResponseDTO;
import com.givebridge.model.Campaign;
import com.givebridge.repository.CampaignRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<CampaignResponseDTO> getAllCampaigns() {
        return campaignRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single campaign by its ID.
     * Throws EntityNotFoundException if the campaign does not exist.
     *
     * @param id the campaign ID
     * @return the found campaign
     */
    public CampaignResponseDTO getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Campaign not found with id: " + id));

        return mapToResponseDTO(campaign);
    }

    /**
     * Creates a new campaign from the provided request data.
     * Maps DTO fields to a Campaign entity and saves to the database.
     *
     * @param dto the campaign request data
     * @return the saved campaign
     */
    public CampaignResponseDTO createCampaign(CampaignRequestDTO dto) {
        Campaign campaign = Campaign.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .goalAmount(dto.getGoalAmount())
                .deadline(dto.getDeadline())
                .build();

        Campaign saved = campaignRepository.save(campaign);
        return mapToResponseDTO(saved);
    }

    /**
     * Updates an existing campaign with new data.
     * Throws EntityNotFoundException if the campaign does not exist.
     *
     * @param id  the ID of the campaign to update
     * @param dto the updated campaign data
     * @return the updated campaign
     */
    public CampaignResponseDTO updateCampaign(Long id, CampaignRequestDTO dto) {
        Campaign existing = getCampaignEntityById(id);

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setGoalAmount(dto.getGoalAmount());
        existing.setDeadline(dto.getDeadline());

        Campaign saved = campaignRepository.save(existing);
        return mapToResponseDTO(saved);
    }

    /**
     * Deletes a campaign by its ID.
     * Throws EntityNotFoundException if the campaign does not exist.
     *
     * @param id the ID of the campaign to delete
     */
    public void deleteCampaign(Long id) {
        Campaign existing = getCampaignEntityById(id);
        campaignRepository.delete(existing);
    }

    /**
     * Internal method for use by other services that need the Campaign entity.
     * Not exposed via controller — returns entity not DTO.
     *
     * @param id the campaign ID
     * @return the Campaign entity
     */
    public Campaign getCampaignEntityById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Campaign not found with id: " + id
                ));
    }

    /**
     * Maps a Campaign entity to a CampaignResponseDTO.
     *
     * @param campaign the campaign entity to map
     * @return the mapped response DTO
     */
    private CampaignResponseDTO mapToResponseDTO(Campaign campaign) {
        return CampaignResponseDTO.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .goalAmount(campaign.getGoalAmount())
                .raisedAmount(campaign.getRaisedAmount())
                .deadline(campaign.getDeadline())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}