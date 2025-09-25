package org.example.application.order;

import jakarta.transaction.Transactional;
import org.example.domain.order.Order;
import org.example.domain.order.OrderRepository;
import org.example.domain.order.OrderStatus;
import org.example.infrastructure.http.PaymentClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    public OrderService(OrderRepository orderRepository, PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public Order submit(String customerEmail, BigDecimal amount) {
        Order order = new Order(null, customerEmail, amount, OrderStatus.PENDING, null);

        boolean authorized = paymentClient.authorize(customerEmail, amount)
                .onErrorReturn(false)
                .blockOptional()
                .orElse(false);
        if (!authorized) {
            order.markFailed();
            throw new RuntimeException("Payment authorization failed");
        }

        order.markSubmitted();
        return orderRepository.save(order);
    }

    public List<Order> list() { return orderRepository.findAll(); }

    public Order get(Long id) { return orderRepository.findById(id).orElse(null); }
}

