package com.givebridge.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a Donation.
 * Contains only the fields the client is allowed to provide.
 * Keeps server-controlled fields like donatedAt and the full
 * Campaign object out of the API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationRequestDTO {

    /**
     * The name of the donor.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The email address of the donor.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    /**
     * An optional message from the donor to the campaign organizer.
     * May be null.
     */
    private String optionalMessage;

    /**
     * The donation amount in USD.
     * Must be at least $1.
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Donation must be at least $1")
    private BigDecimal amount;

    /**
     * The ID of the campaign this donation belongs to.
     * The server uses this to look up the full Campaign entity.
     */
    @NotNull(message = "Campaign ID is required")
    private Long campaignId;
}