package org.example.infrastructure.cb;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerEventLogger {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerEventLogger.class);
    private final CircuitBreakerRegistry registry;

    public CircuitBreakerEventLogger(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void wireUp() {
        registry.getAllCircuitBreakers().forEach(this::attach);
        registry.getEventPublisher().onEntryAdded(event -> attach(event.getAddedEntry()));
    }

    private void attach(CircuitBreaker cb) {
        cb.getEventPublisher()
                .onStateTransition(e -> log.info("CB[{}] {} -> {}", cb.getName(),
                        e.getStateTransition().getFromState(), e.getStateTransition().getToState()))
                .onError(e -> log.warn("CB[{}] errorRecorded: {}", cb.getName(), e.getThrowable().toString()))
                .onSuccess(e -> log.debug("CB[{}] successRecorded", cb.getName()));
    }
}