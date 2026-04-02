package com.givebridge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a fundraising campaign in GiveBridge.
 * <p>
 * Each campaign has a fundraising goal, a deadline, and tracks
 * how much has been raised through donations.
 * </p>
 * Maps to the "campaigns" table in PostgreSQL.
 */
@Entity
@Table(name = "campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    /**
     * Auto-generated unique identifier for the campaign.
     * The database handles incrementing this value.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the campaign.
     * Must not be blank and cannot exceed 100 characters.
     */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be under 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * A detailed description of the campaign.
     * Stored as TEXT in PostgreSQL to support long content.
     */
    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * The fundraising goal amount in USD.
     * Uses BigDecimal instead of double/float to avoid
     * floating point precision issues with currency calculations.
     */
    @NotNull(message = "Goal amount is required")
    @DecimalMin(value = "1.0", message = "Goal must be at least $1")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal goalAmount;

    /**
     * The total amount raised so far through donations.
     * Defaults to 0 when a campaign is first created.
     * Updated each time a new donation is made.
     */
    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal raisedAmount = BigDecimal.ZERO;

    /**
     * The deadline by which the campaign aims to reach its goal.
     * Must be a future date at the time of creation.
     */
    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    @Column(nullable = false)
    private LocalDate deadline;

    /**
     * Timestamp of when the campaign was created.
     * Automatically set by the {@link #onCreate()} method.
     * Cannot be updated after creation.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets the createdAt timestamp before
     * the entity is first saved to the database.
     * Triggered by the JPA @PrePersist lifecycle event.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}