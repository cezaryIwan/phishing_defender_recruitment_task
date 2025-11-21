package com.defender.controller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.defender.model.SmsMessage;
import com.defender.service.MongoService;
import com.defender.service.KafkaService;

import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.io.IOException;

public class SmsController {
    private final HttpServer server;
    private final ObjectMapper objectMapper;
    private final KafkaService kafkaService;
    private final MongoService mongoService;
    private static final String SUBSCRIPTION_NUMBER = "000000000";

    public SmsController(KafkaService kafkaService, MongoService mongoService, ObjectMapper objectMapper) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        this.objectMapper = objectMapper;
        this.kafkaService = kafkaService;
        this.mongoService = mongoService;

        server.createContext("/sms", this::handleSmsRequest);
    }

    private void handleSmsRequest(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        try {
            SmsMessage sms = objectMapper.readValue(exchange.getRequestBody(), SmsMessage.class);
            
            if (SUBSCRIPTION_NUMBER.equals(sms.getReceiver())) {
                handleSubscription(sms);
                sendResponse(exchange, 200, "Subscription updated");
                return;
            }

            kafkaService.sendMessage(sms);
            sendResponse(exchange, 200, "Message accepted");
            
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    private void handleSubscription(SmsMessage sms) {
        switch (sms.getMessage().toUpperCase()) {
            case "START" -> mongoService.subscribe(sms.getSender());
            case "STOP" -> mongoService.unsubscribe(sms.getSender());
            default -> throw new IllegalArgumentException("Invalid subscription command"); //move to service
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}