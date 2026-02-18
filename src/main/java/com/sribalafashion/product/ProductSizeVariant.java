package com.sribalafashion.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Entity
@Table(name = "product_size_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSizeVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "size_label", nullable = false)
    private String sizeLabel;

    @Column(nullable = false)
    private BigDecimal price;
}
