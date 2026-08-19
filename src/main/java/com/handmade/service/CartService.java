package com.handmade.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.handmade.dto.AddToCartRequest;
import com.handmade.entity.CartItem;
import com.handmade.entity.Product;
import com.handmade.entity.User;
import com.handmade.repository.CartItemRepository;
import com.handmade.repository.ProductRepository;
import com.handmade.repository.UserRepository;
import com.handmade.dto.CartItemResponse;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private CartItemResponse toResponse(CartItem cartItem) {

    Product product = cartItem.getProduct();

    return new CartItemResponse(
            cartItem.getId(),
            product.getId(),
            product.getName(),
            product.getPrice(),
            cartItem.getQuantity(),
            product.getImageUrl()
    );
}

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
public CartItemResponse addToCart(
        Long userId,
        AddToCartRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getAvailable()) {
            throw new RuntimeException("Product is not available");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new RuntimeException("Not enough stock available");
        }

        CartItem cartItem = cartItemRepository
                .findByUserAndProduct(user, product)
                .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Not enough stock available");
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem(
                    user,
                    product,
                    request.getQuantity()
            );
        }

       CartItem savedItem = cartItemRepository.save(cartItem);

return toResponse(savedItem);
    }

   public List<CartItemResponse> getCart(Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return cartItemRepository.findByUser(user)
            .stream()
            .map(this::toResponse)
            .toList();
}

    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot remove another user's cart item");
        }

        cartItemRepository.delete(cartItem);
    }
    @Transactional
public void clearCart(Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    cartItemRepository.deleteByUser(user);
}

  @Transactional
public CartItemResponse updateCartQuantity(
        Long userId,
        Long cartItemId,
        Integer quantity) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

    if (!cartItem.getUser().getId().equals(user.getId())) {
        throw new RuntimeException(
                "You cannot update another user's cart item");
    }

    if (quantity == null || quantity <= 0) {
        throw new RuntimeException(
                "Quantity must be greater than zero");
    }

    Product product = cartItem.getProduct();

    if (quantity > product.getStockQuantity()) {
        throw new RuntimeException(
                "Not enough stock available");
    }

    cartItem.setQuantity(quantity);

    CartItem savedItem = cartItemRepository.save(cartItem);

    return toResponse(savedItem);
}
}