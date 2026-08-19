package com.handmade.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import com.razorpay.Utils;
import com.handmade.dto.PaymentResponse;
import com.handmade.entity.Order;
import com.handmade.entity.OrderStatus;
import com.handmade.entity.Payment;
import com.handmade.entity.PaymentStatus;
import com.handmade.repository.OrderRepository;
import com.handmade.repository.PaymentRepository;
import com.razorpay.RazorpayClient;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
private String razorpayKeySecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            RazorpayClient razorpayClient) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.razorpayClient = razorpayClient;
    }

    @Transactional
    public PaymentResponse createPayment(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (paymentRepository.findByOrder(order).isPresent()) {
            throw new RuntimeException(
                    "Payment already exists for this order");
        }

        try {

            int amountInPaise =
                    (int) Math.round(order.getTotalAmount() * 100);

            JSONObject options = new JSONObject();

            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put(
                    "receipt",
                    "order_" + order.getId()
            );

            com.razorpay.Order razorpayOrder =
                    razorpayClient.orders.create(options);

            String razorpayOrderId =
                    razorpayOrder.get("id");

            Payment payment = new Payment(
                    order,
                    order.getTotalAmount(),
                    PaymentStatus.PENDING
            );

            payment.setRazorpayOrderId(razorpayOrderId);

            Payment savedPayment =
                    paymentRepository.save(payment);

            return new PaymentResponse(savedPayment);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create Razorpay order: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(
            Long paymentId,
            PaymentStatus status) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        payment.setStatus(status);

        Order order = payment.getOrder();

        if (status == PaymentStatus.SUCCESS) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        if (status == PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);

        Payment savedPayment =
                paymentRepository.save(payment);

        return new PaymentResponse(savedPayment);
    }

   @Transactional
public PaymentResponse verifyPayment(
        Long paymentId,
        Long userId,
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature) {

    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() ->
                    new RuntimeException("Payment not found"));

    Order order = payment.getOrder();

    // Security: payment must belong to logged-in user
    if (!order.getUser().getId().equals(userId)) {
        throw new RuntimeException(
                "You cannot verify payment for another user's order");
    }

    // Prevent re-verification
    if (payment.getStatus() == PaymentStatus.SUCCESS) {
        throw new RuntimeException(
                "Payment is already successful");
    }

    // Verify Razorpay order ID
    if (!payment.getRazorpayOrderId()
            .equals(razorpayOrderId)) {

        throw new RuntimeException(
                "Razorpay order ID does not match");
    }

    try {

        JSONObject attributes = new JSONObject();

        attributes.put(
                "razorpay_order_id",
                razorpayOrderId);

        attributes.put(
                "razorpay_payment_id",
                razorpayPaymentId);

        attributes.put(
                "razorpay_signature",
                razorpaySignature);

        boolean verified =
                Utils.verifyPaymentSignature(
                        attributes,
                        razorpayKeySecret);

        if (!verified) {

            payment.setStatus(
                    PaymentStatus.FAILED);

            order.setStatus(
                    OrderStatus.CANCELLED);

            orderRepository.save(order);
            paymentRepository.save(payment);

            throw new RuntimeException(
                    "Payment signature verification failed");
        }

        // Payment successfully verified
        payment.setRazorpayPaymentId(
                razorpayPaymentId);

        payment.setRazorpaySignature(
                razorpaySignature);

        payment.setStatus(
                PaymentStatus.SUCCESS);

        order.setStatus(
                OrderStatus.CONFIRMED);

        orderRepository.save(order);

        Payment savedPayment =
                paymentRepository.save(payment);

        return new PaymentResponse(savedPayment);

    } catch (Exception e) {

        throw new RuntimeException(
                "Payment verification failed: "
                        + e.getMessage(),
                e);
    }
}

public PaymentResponse getPaymentByOrder(Long orderId) {

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    Payment payment = paymentRepository.findByOrder(order)
            .orElseThrow(() ->
                    new RuntimeException("Payment not found"));

    return new PaymentResponse(payment);
}
}