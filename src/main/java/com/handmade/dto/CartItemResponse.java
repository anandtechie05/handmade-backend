package com.handmade.dto;

public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private String imageUrl;

    public CartItemResponse() {
    }

    public CartItemResponse(
            Long id,
            Long productId,
            String productName,
            Double price,
            Integer quantity,
            String imageUrl) {

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}