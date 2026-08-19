package com.handmade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.handmade.entity.Order;
import com.handmade.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);
}