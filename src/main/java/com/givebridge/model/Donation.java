package com.givebridge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a donation made to a fundraising campaign in GiveBridge.
 * Each donation is a single transaction — donors are not uniquely tracked
 * across multiple donations (no donor account system).
 * Maps to the "donations" table in PostgreSQL.
 */
@Entity
@Table(name = "donations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Donation {

    /**
     * Auto-generated unique identifier for this donation transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The donor's name. Required for record keeping and thank you messages.
     */
    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * The donor's email address. Used for donation confirmation.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * An optional message from the donor to the campaign organizer.
     * May be null if the donor chooses not to leave a message.
     */
    @Column
    private String optionalMessage;

    /**
     * The donation amount in USD.
     * Uses BigDecimal to avoid floating point precision issues with currency.
     * Must be at least $1.
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Donation must be at least $1")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Timestamp of when the donation was made.
     * Automatically set by the {@link #onCreate()} method.
     * Cannot be updated after creation.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime donatedAt;

    /**
     * The campaign this donation belongs to.
     * Many donations can belong to one campaign (Many-to-One relationship).
     * FetchType.LAZY means campaign data is only loaded when explicitly accessed,
     * not automatically every time a donation is loaded.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    /**
     * Automatically sets the donatedAt timestamp before
     * the entity is first saved to the database.
     * Triggered by the JPA @PrePersist lifecycle event.
     */
    @PrePersist
    protected void onCreate() {
        this.donatedAt = LocalDateTime.now();
    }
}