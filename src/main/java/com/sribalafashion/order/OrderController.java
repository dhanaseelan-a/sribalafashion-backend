package com.sribalafashion.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Customer: place an order
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request, Authentication auth) {
        try {
            OrderResponse order = orderService.placeOrder(auth.getName(), request);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Customer: view own orders
    @GetMapping("/orders/my")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getOrdersByEmail(auth.getName()));
    }

    // Admin: view all orders (paginated)
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> allOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getAllOrdersPaginated(
                org.springframework.data.domain.PageRequest.of(page, size)));
    }

    // Admin: update order status
    @PutMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            OrderResponse order = orderService.updateOrderStatus(id, body.get("status"));
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin: update payment status
    @PutMapping("/admin/orders/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePayment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            OrderResponse order = orderService.updatePaymentStatus(id, body.get("status"));
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin: update estimated delivery
    @PutMapping("/admin/orders/{id}/delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDelivery(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            OrderResponse order = orderService.updateEstimatedDelivery(id, body.get("estimatedDelivery"));
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
