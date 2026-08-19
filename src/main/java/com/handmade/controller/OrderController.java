package com.handmade.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.handmade.dto.OrderResponse;
import com.handmade.entity.Order;
import com.handmade.entity.OrderStatus;
import com.handmade.entity.User;
import com.handmade.repository.UserRepository;
import com.handmade.service.OrderService;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(
            OrderService orderService,
            UserRepository userRepository) {

        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Order order = orderService.createOrder(user.getId());

        return ResponseEntity.ok(new OrderResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                orderService.getUserOrders(user.getId())
                        .stream()
                        .map(OrderResponse::new)
                        .toList()
        );
    }

    @GetMapping("/all")
public ResponseEntity<List<OrderResponse>> getAllOrders(
        Authentication authentication) {

    boolean isManager = authentication.getAuthorities()
            .stream()
            .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_MANAGER"));

    if (!isManager) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }

    return ResponseEntity.ok(
            orderService.getAllOrders()
                    .stream()
                    .map(OrderResponse::new)
                    .toList()
    );
}

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Order order = orderService.getOrder(
                user.getId(),
                orderId
        );

        return ResponseEntity.ok(new OrderResponse(order));
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @PostConstruct
    public void testControllerLoaded() {
        System.out.println("🔥 ORDER CONTROLLER LOADED");
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        boolean isManager = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_MANAGER"));

        Order order;

        // Manager can access any order
        if (isManager) {
            order = orderService.getOrderById(orderId);
        } else {
            // Customer can access only their own order
            order = orderService.getOrder(user.getId(), orderId);
        }

        // =========================
        // CUSTOMER
        // =========================
        if (!isManager) {

            if (status == OrderStatus.CANCELLED) {

                if (order.getStatus() != OrderStatus.PENDING) {
                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body("Only pending orders can be cancelled");
                }

                order.setStatus(OrderStatus.CANCELLED);

                return ResponseEntity.ok(
                        new OrderResponse(
                                orderService.saveOrder(order)
                        )
                );
            }

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Customers cannot change order status to " + status);
        }

        // =========================
        // MANAGER
        // =========================

        if (status == OrderStatus.CONFIRMED) {

            if (order.getStatus() != OrderStatus.PENDING) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Only pending orders can be confirmed");
            }

            order.setStatus(OrderStatus.CONFIRMED);

        } else if (status == OrderStatus.SHIPPED) {

            if (order.getStatus() != OrderStatus.CONFIRMED) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Only confirmed orders can be shipped");
            }

            order.setStatus(OrderStatus.SHIPPED);

        } else if (status == OrderStatus.DELIVERED) {

            if (order.getStatus() != OrderStatus.SHIPPED) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Only shipped orders can be delivered");
            }

            order.setStatus(OrderStatus.DELIVERED);

        } else if (status == OrderStatus.CANCELLED) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Manager cannot cancel orders using this endpoint");

        } else {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid order status");
        }

        // Save manager's status change
        return ResponseEntity.ok(
                new OrderResponse(
                        orderService.saveOrder(order)
                )
        );

        
    }
}