package com.handmade.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.handmade.entity.Order;

public class OrderResponse {

    private Long id;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    // Customer details
    private Long customerId;
    private String customerName;
    private String customerEmail;

    private List<OrderItemResponse> items;

    public OrderResponse(Order order) {

        this.id = order.getId();

        this.totalAmount = order.getTotalAmount();

        this.status = order.getStatus().name();

        this.createdAt = order.getCreatedAt();

        // Customer information
        if (order.getUser() != null) {

            this.customerId = order.getUser().getId();

            this.customerName = order.getUser().getName();

            this.customerEmail = order.getUser().getEmail();
        }

        this.items = order.getItems()
                .stream()
                .map(OrderItemResponse::new)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}