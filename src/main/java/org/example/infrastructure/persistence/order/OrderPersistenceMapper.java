package org.example.infrastructure.persistence.order;

import org.example.domain.order.Order;
import org.example.domain.order.OrderStatus;

public final class OrderPersistenceMapper {
    private OrderPersistenceMapper() { }

    public static OrderEntity toEntity(Order order) {
        OrderEntity e = new OrderEntity();
        e.setId(order.getId());
        e.setCustomerEmail(order.getCustomerEmail());
        e.setAmount(order.getAmount());
        e.setStatus(order.getStatus().name());
        e.setCreatedAt(order.getCreatedAt());
        return e;
    }

    public static Order toDomain(OrderEntity e) {
        return new Order(e.getId(), e.getCustomerEmail(), e.getAmount(), OrderStatus.valueOf(e.getStatus()), e.getCreatedAt());
    }
}

