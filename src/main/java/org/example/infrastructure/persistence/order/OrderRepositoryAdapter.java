package org.example.infrastructure.persistence.order;

import org.example.domain.order.Order;
import org.example.domain.order.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity saved = jpaRepository.save(OrderPersistenceMapper.toEntity(order));
        return OrderPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(OrderPersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream().map(OrderPersistenceMapper::toDomain).toList();
    }
}

