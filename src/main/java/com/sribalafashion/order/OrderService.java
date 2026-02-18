package com.sribalafashion.order;

import com.sribalafashion.product.Product;
import com.sribalafashion.product.ProductRepository;
import com.sribalafashion.user.User;
import com.sribalafashion.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

        private final OrderRepository orderRepository;
        private final ProductRepository productRepository;
        private final UserRepository userRepository;

        @Transactional
        public OrderResponse placeOrder(String email, OrderRequest request) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Build order
                Order order = Order.builder()
                                .userId(user.getUserId())
                                .customerName(request.getCustomerName())
                                .customerEmail(email)
                                .phone(request.getPhone())
                                .address(request.getAddress())
                                .city(request.getCity())
                                .state(request.getState())
                                .pincode(request.getPincode())
                                .transactionId(request.getTransactionId())
                                .paymentMethod("UPI")
                                .totalAmount(BigDecimal.ZERO)
                                .build();

                BigDecimal total = BigDecimal.ZERO;
                java.util.List<Product> modifiedProducts = new java.util.ArrayList<>();

                for (OrderRequest.ItemRequest itemReq : request.getItems()) {
                        Product product = productRepository.findById(itemReq.getProductId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Product not found: " + itemReq.getProductId()));

                        // Validate stock
                        if (product.getStock() < itemReq.getQuantity()) {
                                throw new RuntimeException("Insufficient stock for " + product.getName()
                                                + ". Available: " + product.getStock());
                        }

                        // Determine unit price: use size variant price if a size is selected
                        BigDecimal unitPrice = product.getPrice();
                        String selectedSize = itemReq.getSelectedSize();
                        if (selectedSize != null && !selectedSize.isEmpty() && product.getSizeVariants() != null) {
                                unitPrice = product.getSizeVariants().stream()
                                                .filter(v -> v.getSizeLabel().equals(selectedSize))
                                                .findFirst()
                                                .map(com.sribalafashion.product.ProductSizeVariant::getPrice)
                                                .orElse(product.getPrice());
                        }

                        // Calculate price with discount
                        int discount = product.getDiscountPercent() != null ? product.getDiscountPercent() : 0;
                        BigDecimal finalPrice;
                        if (discount > 0) {
                                finalPrice = unitPrice.multiply(BigDecimal.valueOf(100 - discount))
                                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        } else {
                                finalPrice = unitPrice;
                        }

                        BigDecimal lineTotal = finalPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                        OrderItem item = OrderItem.builder()
                                        .order(order)
                                        .productId(product.getId())
                                        .productName(product.getName())
                                        .quantity(itemReq.getQuantity())
                                        .unitPrice(unitPrice)
                                        .discountPercent(discount)
                                        .finalPrice(finalPrice)
                                        .selectedSize(selectedSize)
                                        .build();

                        order.getItems().add(item);
                        total = total.add(lineTotal);

                        // Decrement stock (batch save below)
                        product.setStock(product.getStock() - itemReq.getQuantity());
                        modifiedProducts.add(product);
                }

                // Batch save all stock updates in one round-trip
                productRepository.saveAll(modifiedProducts);
                order.setTotalAmount(total);
                Order saved = orderRepository.save(order);
                return OrderResponse.fromEntity(saved);
        }

        @Transactional(readOnly = true)
        public List<OrderResponse> getOrdersByEmail(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId())
                                .stream()
                                .map(OrderResponse::fromEntity)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<OrderResponse> getAllOrders() {
                return orderRepository.findAllByOrderByCreatedAtDesc()
                                .stream()
                                .map(OrderResponse::fromEntity)
                                .toList();
        }

        @Transactional(readOnly = true)
        public Page<OrderResponse> getAllOrdersPaginated(Pageable pageable) {
                return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                                .map(OrderResponse::fromEntity);
        }

        @Transactional
        public OrderResponse updateOrderStatus(Long orderId, String status) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));
                order.setOrderStatus(Order.OrderStatus.valueOf(status));
                return OrderResponse.fromEntity(orderRepository.save(order));
        }

        @Transactional
        public OrderResponse updatePaymentStatus(Long orderId, String status) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));
                Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(status);
                order.setPaymentStatus(paymentStatus);
                // Auto-cancel order when payment fails
                if (paymentStatus == Order.PaymentStatus.FAILED) {
                        order.setOrderStatus(Order.OrderStatus.CANCELLED);
                }
                return OrderResponse.fromEntity(orderRepository.save(order));
        }

        @Transactional
        public OrderResponse updateEstimatedDelivery(Long orderId, String estimatedDelivery) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));
                order.setEstimatedDelivery(estimatedDelivery);
                return OrderResponse.fromEntity(orderRepository.save(order));
        }
}
