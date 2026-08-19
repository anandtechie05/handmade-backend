package com.handmade.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.handmade.entity.CartItem;
import com.handmade.entity.User;
import com.handmade.entity.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    java.util.List<CartItem> findByUser(User user);

    void deleteByUser(User user);
}