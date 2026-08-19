package com.handmade.dto;

import com.handmade.entity.Payment;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private Double amount;
    private String status;
    private String razorpayOrderId;
    private String razorpayPaymentId;

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.orderId = payment.getOrder().getId();
        this.amount = payment.getAmount();
        this.status = payment.getStatus().name();
        this.razorpayOrderId = payment.getRazorpayOrderId();
        this.razorpayPaymentId = payment.getRazorpayPaymentId();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }
}