package com.handmade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.handmade.dto.AddToCartRequest;
import com.handmade.dto.CartItemResponse;
import com.handmade.dto.UpdateCartRequest;
import com.handmade.entity.User;
import com.handmade.repository.UserRepository;
import com.handmade.service.CartService;


@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(
            CartService cartService,
            UserRepository userRepository) {

        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestBody AddToCartRequest request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        CartItemResponse cartItem =
        cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok(cartItem);
    }

   @GetMapping
public ResponseEntity<List<CartItemResponse>> getCart(
        Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                cartService.getCart(user.getId())
        );
    }
    @DeleteMapping("/clear")
public ResponseEntity<?> clearCart(
        Authentication authentication) {

    User user = getAuthenticatedUser(authentication);

    cartService.clearCart(user.getId());

    return ResponseEntity.ok("Cart cleared successfully");
}

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        cartService.removeFromCart(
                user.getId(),
                cartItemId
        );

        return ResponseEntity.ok("Item removed from cart");
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @PutMapping("/{cartItemId}")
public ResponseEntity<?> updateCartQuantity(
        @PathVariable Long cartItemId,
        @RequestBody UpdateCartRequest request,
        Authentication authentication) {

    User user = getAuthenticatedUser(authentication);

   CartItemResponse updatedItem = cartService.updateCartQuantity(
        user.getId(),
        cartItemId,
        request.getQuantity()
);

    return ResponseEntity.ok(updatedItem);
}
}