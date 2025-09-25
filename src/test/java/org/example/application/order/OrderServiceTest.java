package org.example.application.order;

import org.example.domain.order.Order;
import org.example.domain.order.OrderRepository;
import org.example.domain.order.OrderStatus;
import org.example.infrastructure.http.PaymentClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void submit_persists_when_payment_authorized() {
        when(paymentClient.authorize(anyString(), any())).thenReturn(Mono.just(true));
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        Order created = orderService.submit("user@example.com", new BigDecimal("10.00"));

        assertNotNull(created);
        assertEquals(OrderStatus.SUBMITTED, created.getStatus());
        verify(orderRepository).save(any());
    }

    @Test
    void submit_throws_when_payment_fails() {
        when(paymentClient.authorize(anyString(), any())).thenReturn(Mono.just(false));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.submit("user@example.com", new BigDecimal("10.00"))
        );
        assertTrue(ex.getMessage().contains("Payment authorization failed"));
        verify(orderRepository, never()).save(any());
    }
}

