package com.defender.controller;

import com.defender.service.KafkaService;
import com.defender.service.MongoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsControllerTest {
    @Mock
    private KafkaService kafkaService;

    @Mock
    private MongoService mongoService;

    @Mock
    private HttpServer httpServer;

    @Mock
    private HttpExchange exchange;

    @Mock
    private Headers responseHeaders;

    private SmsController controller;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() throws IOException {
        responseBody = new ByteArrayOutputStream();
        controller = new SmsController(kafkaService, mongoService, new ObjectMapper(), httpServer);
        verify(httpServer).createContext(eq("/sms"), any());
    }

    @Test
    void shouldHandleSubscriptionMessage() throws IOException {
        // given
        String json = "{\"sender\":\"48123456789\",\"receiver\":\"000000000\",\"message\":\"START\"}";
        setupHttpExchange(json);

        // when
        controller.handleSmsRequest(exchange);

        // then
        verify(mongoService).subscribe("48123456789");
        verify(exchange).sendResponseHeaders(200, "Subscription updated".length());
        assertEquals("Subscription updated", responseBody.toString());
    }

    @Test
    void shouldHandleUnsubscriptionMessage() throws IOException {
        // given
        String json = "{\"sender\":\"48123456789\",\"receiver\":\"000000000\",\"message\":\"STOP\"}";
        setupHttpExchange(json);

        // when
        controller.handleSmsRequest(exchange);

        // then
        verify(mongoService).unsubscribe("48123456789");
        verify(exchange).sendResponseHeaders(200, "Subscription updated".length());
        assertEquals("Subscription updated", responseBody.toString());
    }

    @Test
    void shouldHandleNormalMessage() throws IOException {
        // given
        String json = "{\"sender\":\"48123456789\",\"receiver\":\"48987654321\",\"message\":\"Hello\"}";
        setupHttpExchange(json);

        // when
        controller.handleSmsRequest(exchange);

        // then
        verify(kafkaService).sendMessage(any());
        verify(exchange).sendResponseHeaders(200, "Message accepted".length());
        assertEquals("Message accepted", responseBody.toString());
    }

    @Test
    void shouldReturn405ForNonPostRequests() throws IOException {
        // given
        when(exchange.getRequestMethod()).thenReturn("GET");

        // when
        controller.handleSmsRequest(exchange);

        // then
        verify(exchange).sendResponseHeaders(405, -1);
        verify(exchange).close();
    }

    @Test
    void shouldHandle400ForInvalidJson() throws IOException {
        // given
        String invalidJson = "not a json";
        setupHttpExchange(invalidJson);

        // when
        controller.handleSmsRequest(exchange);

        // then
        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString().contains("Invalid request"));
    }

    @Test
    void shouldHandle400ForInvalidSubscriptionCommand() throws IOException {
        // given
        String json = "{\"sender\":\"48123456789\",\"receiver\":\"000000000\",\"message\":\"INVALID\"}";
        setupHttpExchange(json);

        // when
        controller.handleSmsRequest(exchange);

        // then
        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        assertTrue(responseBody.toString().contains("Invalid subscription command"));
    }

    private void setupHttpExchange(String json) {
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
    }
}