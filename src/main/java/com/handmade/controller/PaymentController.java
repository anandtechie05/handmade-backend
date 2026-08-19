package com.handmade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.handmade.dto.PaymentResponse;
import com.handmade.entity.Order;
import com.handmade.entity.OrderStatus;
import com.handmade.entity.User;
import com.handmade.repository.OrderRepository;
import com.handmade.repository.UserRepository;
import com.handmade.service.PaymentService;
import com.handmade.dto.PaymentVerificationRequest;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public PaymentController(
            PaymentService paymentService,
            UserRepository userRepository,
            OrderRepository orderRepository) {

        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<?> createPayment(
            @PathVariable Long orderId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You cannot create payment for another user's order");
        }

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Order is already confirmed");
        }

        PaymentResponse payment =
                paymentService.createPayment(orderId);

        return ResponseEntity.ok(payment);
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

  @PostMapping("/{paymentId}/verify")
public ResponseEntity<?> verifyPayment(
        @PathVariable Long paymentId,
        @RequestBody PaymentVerificationRequest request,
        Authentication authentication) {

    User user = getAuthenticatedUser(authentication);

    PaymentResponse payment =
            paymentService.verifyPayment(
                    paymentId,
                    user.getId(),
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

    return ResponseEntity.ok(payment);
}

@GetMapping("/{orderId}")
public ResponseEntity<?> getPayment(
        @PathVariable Long orderId,
        Authentication authentication) {

    User user = getAuthenticatedUser(authentication);

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    if (!order.getUser().getId().equals(user.getId())) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("You cannot access another user's payment");
    }

    return ResponseEntity.ok(
            paymentService.getPaymentByOrder(orderId)
    );
}
}