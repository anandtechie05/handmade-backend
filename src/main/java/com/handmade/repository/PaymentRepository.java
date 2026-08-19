package com.handmade.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.handmade.entity.Payment;
import com.handmade.entity.Order;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

}