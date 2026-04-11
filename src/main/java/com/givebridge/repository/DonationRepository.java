package com.givebridge.repository;

import com.givebridge.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Donation database operations.
 * Extends JpaRepository to inherit standard CRUD operations.
 * Spring Data JPA automatically generates the implementation at runtime.
 */
@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    /**
     * Finds all donations ordered by donated date, newest first.
     * Spring Data JPA auto-generates the SQL from the method name.
     */
    List<Donation> findAllByOrderByDonatedAtDesc();

    /**
     * Finds all donations for a specific campaign ordered by newest first.
     * Spring Data JPA auto-generates the SQL from the method name.
     */
    List<Donation> findByCampaignIdOrderByDonatedAtDesc(Long campaignId);
}
