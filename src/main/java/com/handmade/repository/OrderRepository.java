package com.handmade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.handmade.entity.Order;
import com.handmade.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}