package org.example.infrastructure.http;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);
    private final RestClient restClient;

    public PaymentClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @CircuitBreaker(name = "payment", fallbackMethod = "authorizeFallback")
    public boolean authorize(String customerEmail, BigDecimal amount) {
        try {
            restClient.post()
                    .uri("http://localhost:8081/api/payments/authorize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaymentRequest(customerEmail, amount))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException ex) {
            // Non-2xx
            log.warn("Payment API non-2xx: status={}, body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("payment-non-2xx", ex);
        } catch (Exception ex) {
            // Transport or other errors
            throw new RuntimeException("payment-call-failed", ex);
        }
    }

    // fallback must accept same args + Throwable as last param
    private boolean authorizeFallback(String customerEmail, BigDecimal amount, Throwable t) {
        log.warn("Payment authorize fallback: email={}, amount={}, cause={}", customerEmail, amount, t.toString());
        return false;
    }

    public record PaymentRequest(String customerEmail, BigDecimal amount) {}
}

