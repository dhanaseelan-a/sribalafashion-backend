package com.sribalafashion.order;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer discountPercent;
    private BigDecimal finalPrice;
    private String selectedSize;
}
