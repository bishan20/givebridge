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

@Entity
@Table(name = "campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must me under 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Goal amount is required")
    @DecimalMin(value = "1.0", message = "Goal must be at least $1")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal goalAmount;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal raisedAmount = BigDecimal.ZERO;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
