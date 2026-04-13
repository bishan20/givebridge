package com.givebridge.service;

import com.givebridge.dto.DonationRequestDTO;
import com.givebridge.dto.DonationResponseDTO;
import com.givebridge.model.Campaign;
import com.givebridge.model.Donation;
import com.givebridge.repository.DonationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final CampaignService campaignService;

    /**
     * Retrieves all donations ordered by donated date, newest first.
     * @return list of all donations as response DTOs
     */
    public List<DonationResponseDTO> getAllDonations() {
        return donationRepository.findAllByOrderByDonatedAtDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all donations for a specific campaign.
     * Throws EntityNotFoundException if the campaign does not exist.
     *
     * @param id the campaign ID
     * @return list of all donations for the campaign as response DTOs
     */
    public List<DonationResponseDTO> getDonationsByCampaignId(Long id) {
        Campaign existing = campaignService.getCampaignById(id);
        return donationRepository.findByCampaignIdOrderByDonatedAtDesc(existing.getId())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single donation by its ID.
     * Throws EntityNotFoundException if the donation does not exist.
     *
     * @param id the donation ID
     * @return the found donation as a response DTO
     */
    public DonationResponseDTO getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Donation not found with id " + id
                ));
        return mapToResponseDTO(donation);
    }

    /**
     * Creates a new donation from the provided request data.
     * Updates the campaign raisedAmount atomically via @Transactional.
     *
     * @param dto the donation request data
     * @return the saved donation as a response DTO
     */
    @Transactional
    public DonationResponseDTO createDonation(DonationRequestDTO dto) {
        Campaign existing = campaignService.getCampaignById(dto.getCampaignId());

        if (existing.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("This campaign is due as of " + existing.getDeadline());
        }

        BigDecimal updatedRaisedAmount = existing.getRaisedAmount().add(dto.getAmount());
        existing.setRaisedAmount(updatedRaisedAmount);

        Donation donation = Donation.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .optionalMessage(dto.getOptionalMessage())
                .amount(dto.getAmount())
                .campaign(existing)
                .build();

        Donation saved = donationRepository.save(donation);
        return mapToResponseDTO(saved);
    }

    /**
     * Maps a Donation entity to a DonationResponseDTO.
     * Extracts only campaignId and campaignTitle from the Campaign
     * to avoid lazy loading issues and N+1 queries.
     *
     * @param donation the donation entity to map
     * @return the mapped response DTO
     */
    private DonationResponseDTO mapToResponseDTO(Donation donation) {
        return DonationResponseDTO.builder()
                .id(donation.getId())
                .name(donation.getName())
                .email(donation.getEmail())
                .optionalMessage(donation.getOptionalMessage())
                .amount(donation.getAmount())
                .donatedAt(donation.getDonatedAt())
                .campaignId(donation.getCampaign().getId())
                .campaignTitle(donation.getCampaign().getTitle())
                .build();
    }
}
