package com.givebridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning Donation data in API responses.
 * Avoids exposing the full Campaign entity in the response,
 * preventing Jackson serialization issues with lazy-loaded proxies
 * and the N+1 query problem.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponseDTO {

    /** The unique identifier of the donation. */
    private Long id;

    /** The donor's name. */
    private String name;

    /** The donor's email address. */
    private String email;

    /** An optional message from the donor. */
    private String optionalMessage;

    /** The donation amount in USD. */
    private BigDecimal amount;

    /** Timestamp of when the donation was made. */
    private LocalDateTime donatedAt;

    /** The ID of the campaign this donation belongs to. */
    private Long campaignId;

    /** The title of the campaign this donation belongs to. */
    private String campaignTitle;
}