package org.example.infrastructure.http;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @CircuitBreaker(name = "payment", fallbackMethod = "authorizeFallback")
    public Mono<Boolean> authorize(String customerEmail, BigDecimal amount) {
        return webClient.post()
                .uri("http://localhost:8081/api/payments/authorize")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PaymentRequest(customerEmail, amount))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), resp -> Mono.error(new RuntimeException("payment-non-2xx")))
                .toBodilessEntity()
                .map(e -> true);
    }

    // fallback must accept same args + Throwable as last param
    private Mono<Boolean> authorizeFallback(String customerEmail, BigDecimal amount, Throwable t) {
        return Mono.just(false);
    }
    public record PaymentRequest(String customerEmail, BigDecimal amount) {}
}

