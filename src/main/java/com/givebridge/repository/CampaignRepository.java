package com.givebridge.repository;

import com.givebridge.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Campaign database operations.
 * Extends JpaRepository to inherit standard CRUD operations.
 * Spring Data JPA automatically generates the implementation at runtime.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * Finds all campaigns ordered by creation date, newest first.
     * Spring Data JPA auto-generates the SQL from the method name.
     */
    List<Campaign> findAllByOrderByCreatedAtDesc();
}