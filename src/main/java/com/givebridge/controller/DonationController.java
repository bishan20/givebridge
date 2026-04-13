package com.givebridge.controller;

import com.givebridge.dto.DonationRequestDTO;
import com.givebridge.dto.DonationResponseDTO;
import com.givebridge.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    /**
     * GET /api/donations
     * Retrieves all donations ordered by newest first.
     *
     * @return list of all donations with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<DonationResponseDTO>> getAllDonations() {
        List<DonationResponseDTO> donations = donationService.getAllDonations();
        return ResponseEntity.ok(donations);
    }

    /**
     * GET /api/donations/campaign/{id}
     * Retrieves donations for a particular campaign.
     *
     * @param id the campaign ID from the URL path
     * @return the donations with HTTP 200 OK, or 404 if not found
     */
    @GetMapping("/campaign/{id}")
    public ResponseEntity<List<DonationResponseDTO>> getDonationsByCampaignId(@PathVariable Long id) {
        List<DonationResponseDTO> donations = donationService.getDonationsByCampaignId(id);
        return ResponseEntity.ok(donations);
    }

    /**
     * GET /api/donations/{id}
     * Retrieves a single donation by its ID.
     *
     * @param id the donation ID from the URL path
     * @return the donation with HTTP 200 OK, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DonationResponseDTO> getDonationById(@PathVariable Long id) {
        DonationResponseDTO donation = donationService.getDonationById(id);
        return ResponseEntity.ok(donation);
    }

    /**
     * POST /api/donations
     * Creates a new donation.
     *
     * @param dto the donation data from the request body
     * @return the created donation with HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<DonationResponseDTO> createDonation(@Valid @RequestBody DonationRequestDTO dto) {
        DonationResponseDTO created = donationService.createDonation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
