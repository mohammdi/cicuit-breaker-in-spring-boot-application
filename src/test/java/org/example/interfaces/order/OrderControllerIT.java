package org.example.interfaces.order;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIT {

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) wireMockServer.stop();
    }

    @Test
    void submit_order_success_when_payment_2xx() throws Exception {
        wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/payments/authorize"))
                .willReturn(aResponse().withStatus(200)));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerEmail\":\"it@example.com\",\"amount\":25.00}"))
                .andExpect(status().isCreated());
    }

    @Test
    void submit_order_fails_when_payment_5xx_and_circuit_opens() throws Exception {
        wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/payments/authorize"))
                .willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"customerEmail\":\"it@example.com\",\"amount\":25.00}"))
                    .andExpect(status().isBadGateway());
        }

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerEmail\":\"it@example.com\",\"amount\":25.00}"))
                .andExpect(status().isBadGateway());
    }
}
