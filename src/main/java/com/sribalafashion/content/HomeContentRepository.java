package com.sribalafashion.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HomeContentRepository extends JpaRepository<HomeContent, Long> {

    // Direct single-row query instead of loading ALL rows then streaming
    @Query("SELECT h FROM HomeContent h ORDER BY h.id ASC LIMIT 1")
    Optional<HomeContent> findFirst();
}
