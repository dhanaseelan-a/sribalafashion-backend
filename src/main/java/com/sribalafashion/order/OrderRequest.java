package com.sribalafashion.order;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String customerName;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String transactionId;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private Long productId;
        private Integer quantity;
        private String selectedSize;
    }
}
