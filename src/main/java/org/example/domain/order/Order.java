package org.example.domain.order;

import org.apache.commons.lang3.RandomUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Order {
    private final Long id;
    private final String customerEmail;
    private final BigDecimal amount;
    private final Instant createdAt;
    private OrderStatus status;

    public Order(Long id, String customerEmail, BigDecimal amount, OrderStatus status, Instant createdAt) {
        this.id = id == null ? RandomUtils.nextLong() : id;
        this.customerEmail = Objects.requireNonNull(customerEmail, "customerEmail");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public Long getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public BigDecimal getAmount() { return amount; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void markSubmitted() { this.status = OrderStatus.SUBMITTED; }
    public void markFailed() { this.status = OrderStatus.FAILED; }
}

