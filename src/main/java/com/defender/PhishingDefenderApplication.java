package com.defender;

import com.defender.config.ConfigProvider;
import com.defender.controller.SmsController;
import com.defender.service.KafkaService;
import com.defender.service.MongoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClients;
import com.sun.net.httpserver.HttpServer;


import java.io.IOException;
import java.net.InetSocketAddress;

public class PhishingDefenderApplication {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String connectionString = String.format("mongodb://%s:%d",
                ConfigProvider.getMongoHost(),
                ConfigProvider.getMongoPort());
        MongoService mongoService = new MongoService(MongoClients.create(connectionString), objectMapper);

        KafkaService kafkaService = new KafkaService(mongoService, objectMapper, false);
        SmsController smsController = new SmsController(kafkaService, mongoService, objectMapper, HttpServer.create(new InetSocketAddress(8080), 0));

        kafkaService.start();
        smsController.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down application...");
            smsController.stop();
            kafkaService.stop();
            mongoService.close();
            System.out.println("Application shutdown complete.");
        }));
        System.out.println("PhishingDefender application started successfully.");
    }
}