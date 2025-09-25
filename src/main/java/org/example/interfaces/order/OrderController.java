package org.example.interfaces.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.example.application.order.OrderService;
import org.example.domain.order.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public record SubmitOrderRequest(@NotBlank @Email String customerEmail, @Positive BigDecimal amount) {}

    @PostMapping
    public ResponseEntity<Order> submit(@Valid @RequestBody SubmitOrderRequest req) {
        Order created = orderService.submit(req.customerEmail(), req.amount());
        return ResponseEntity.created(URI.create("/api/orders/" + created.getId())).body(created);
    }

    @GetMapping
    public List<Order> list() { return orderService.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        Order order = orderService.get(id);
        return order == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(order);
    }
}

