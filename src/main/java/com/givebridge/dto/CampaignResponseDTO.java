package com.givebridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning Campaign data in API responses.
 * Avoids exposing the full Campaign entity in the response,
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponseDTO {

    /** The unique identifier of the campaign. */
    private Long id;

    /** The campaign's title. */
    private String title;

    /** The campaign's description. */
    private String description;

    /** The goal amount in USD. */
    private BigDecimal goalAmount;

    /** The raised amount in USD. */
    private BigDecimal raisedAmount;

    /** The deadline of the campaign. */
    private LocalDate deadline;

    /** Timestamp of when the campaign was created. */
    private LocalDateTime createdAt;
}
