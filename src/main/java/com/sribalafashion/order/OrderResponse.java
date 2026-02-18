package com.sribalafashion.order;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
        private Long id;
        private Long userId;
        private String customerName;
        private String customerEmail;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String transactionId;
        private String estimatedDelivery;
        private String paymentMethod;
        private String paymentStatus;
        private String orderStatus;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
        private List<OrderItemResponse> items;

        public static OrderResponse fromEntity(Order order) {
                return OrderResponse.builder()
                                .id(order.getId())
                                .userId(order.getUserId())
                                .customerName(order.getCustomerName())
                                .customerEmail(order.getCustomerEmail())
                                .phone(order.getPhone())
                                .address(order.getAddress())
                                .city(order.getCity())
                                .state(order.getState())
                                .pincode(order.getPincode())
                                .transactionId(order.getTransactionId())
                                .estimatedDelivery(order.getEstimatedDelivery())
                                .paymentMethod(order.getPaymentMethod())
                                .paymentStatus(order.getPaymentStatus().name())
                                .orderStatus(order.getOrderStatus().name())
                                .totalAmount(order.getTotalAmount())
                                .createdAt(order.getCreatedAt())
                                .items(order.getItems().stream()
                                                .map(item -> OrderItemResponse.builder()
                                                                .productId(item.getProductId())
                                                                .productName(item.getProductName())
                                                                .quantity(item.getQuantity())
                                                                .unitPrice(item.getUnitPrice())
                                                                .discountPercent(item.getDiscountPercent())
                                                                .finalPrice(item.getFinalPrice())
                                                                .selectedSize(item.getSelectedSize())
                                                                .build())
                                                .toList())
                                .build();
        }
}
