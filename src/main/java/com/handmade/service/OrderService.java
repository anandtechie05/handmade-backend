package com.handmade.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.handmade.entity.CartItem;
import com.handmade.entity.Order;
import com.handmade.entity.OrderItem;
import com.handmade.entity.OrderStatus;
import com.handmade.entity.Product;
import com.handmade.entity.User;
import com.handmade.repository.CartItemRepository;
import com.handmade.repository.OrderItemRepository;
import com.handmade.repository.OrderRepository;
import com.handmade.repository.ProductRepository;
import com.handmade.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<CartItem> cartItems =
                cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (!product.getAvailable()) {
                throw new RuntimeException(
                        "Product is not available: "
                                + product.getName());
            }

            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new RuntimeException(
                        "Not enough stock for: "
                                + product.getName());
            }

            totalAmount +=
                    product.getPrice() * cartItem.getQuantity();
        }

        Order order = new Order(
                user,
                totalAmount,
                OrderStatus.PENDING
        );

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

           

            OrderItem orderItem = new OrderItem(
        savedOrder,
        product,
        cartItem.getQuantity(),
        product.getPrice()
);

orderItemRepository.save(orderItem);

savedOrder.getItems().add(orderItem);

            product.setStockQuantity(
                    product.getStockQuantity()
                            - cartItem.getQuantity()
            );

            if (product.getStockQuantity() == 0) {
                product.setAvailable(false);
            }

            productRepository.save(product);
        }

        cartItemRepository.deleteByUser(user);

        return savedOrder;
    }

   public List<Order> getUserOrders(Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    return orderRepository.findByUser(user);
}

@Transactional(readOnly = true)
public List<Order> getAllOrders() {
    return orderRepository.findAll();
}

    

       public Order getOrder(Long userId, Long orderId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You cannot access another user's order");
        }

        return order;
    }

    // Used by Manager to access any customer's order
    public Order getOrderById(Long orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}