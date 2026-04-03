package com.givebridge.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating or updating a Campaign.
 * Contains only the fields the client is allowed to provide.
 * Keeps internal fields like raisedAmount and createdAt
 * out of the API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRequestDTO {

    /**
     * The title of the campaign.
     */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be under 100 characters")
    private String title;

    /**
     * A detailed description of the campaign.
     */
    @NotBlank(message = "Description is required")
    private String description;

    /**
     * The fundraising goal amount in USD.
     */
    @NotNull(message = "Goal amount is required")
    @DecimalMin(value = "1.0", message = "Goal must be at least $1")
    private BigDecimal goalAmount;

    /**
     * The deadline by which the campaign aims to reach its goal.
     */
    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;
}