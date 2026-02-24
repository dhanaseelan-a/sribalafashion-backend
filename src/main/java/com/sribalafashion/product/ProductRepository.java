package com.sribalafashion.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);

    // Fetch product WITH size variants in a single query (avoids N+1)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.sizeVariants WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(Long id);

    // Fetch all products WITH size variants in a single query
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.sizeVariants")
    List<Product> findAllWithVariants();

    // Fetch by category WITH size variants in a single query
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.sizeVariants WHERE p.category = :category")
    List<Product> findByCategoryWithVariants(String category);
}
