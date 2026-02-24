package com.sribalafashion.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    // Load orders WITH items in a single query (avoids N+1 for admin panel)
    @EntityGraph(attributePaths = { "items" })
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
