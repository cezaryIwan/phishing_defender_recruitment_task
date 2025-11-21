package com.defender.service;

import com.defender.model.SmsMessage;
import com.defender.config.ConfigProvider;
import com.defender.util.UrlExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.net.http.HttpClient;
import java.util.Properties;

public class KafkaService {
    private static final String INPUT_TOPIC = "sms-messages";

    private final KafkaStreams streams;
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private final MongoService mongoService;
    private final PhishingDetectorService phishingDetector;

    public KafkaService(MongoService mongoService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.mongoService = mongoService;
        this.phishingDetector = new PhishingDetectorService(HttpClient.newHttpClient(), new UrlExtractor());

        Properties streamProps = new Properties();
        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "phishing-detector");
        streamProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigProvider.getKafkaBootstrapServers());
        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                org.apache.kafka.common.serialization.Serdes.StringSerde.class);
        streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                org.apache.kafka.common.serialization.Serdes.StringSerde.class);

        System.out.println("Kafka config: " + streamProps);
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                ConfigProvider.getKafkaBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder);

        this.streams = new KafkaStreams(builder.build(), streamProps);
        this.producer = new KafkaProducer<>(producerProps);
    }

    private void buildTopology(StreamsBuilder builder) {
        System.out.println("Building Kafka topology...");
        KStream<String, String> inputStream = builder.stream(INPUT_TOPIC);

        inputStream
                .peek((key, value) -> System.out.println("Received message: " + value))
                .mapValues(this::parseMessage)
                .peek((key, sms) -> System.out.println("Parsed message: " + sms))
                .filter((key, sms) -> {
                    boolean isSubscribed = mongoService.isSubscribed(sms.getReceiver());
                    System.out.println("Is subscribed check: " + isSubscribed);
                    return isSubscribed;
                })
                .filter((key, sms) -> {
                    boolean isPhishing = !phishingDetector.isPhishing(sms);
                    System.out.println("Is phishing check: " + !isPhishing);
                    return isPhishing;
                })
                .foreach((key, sms) -> {
                    System.out.println("Storing message in MongoDB: " + sms);
                    mongoService.storeMessage(sms);
                });
    }

    public void sendMessage(SmsMessage sms) {
        try {
            String json = objectMapper.writeValueAsString(sms);
            System.out.println("Sending message to Kafka: " + json);
            if (!mongoService.isSubscribed(sms.getReceiver())) {
                System.out.println("Receiver not subscribed, storing directly in MongoDB");
                mongoService.storeMessage(sms);
            } else {
                System.out.println("Receiver subscribed, sending to Kafka");
                producer.send(new ProducerRecord<>(INPUT_TOPIC, sms.getReceiver(), json));
            }
        } catch (Exception e) {
            System.out.println("Error processing message: " + e.getMessage());
            throw new RuntimeException("Failed to process message", e);
        }
    }

    private SmsMessage parseMessage(String json) {
        try {
            return objectMapper.readValue(json, SmsMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SMS message", e);
        }
    }

    public void start() {
        System.out.println("Starting Kafka Streams...");
        streams.start();
        System.out.println("Kafka Streams started");
    }

    public void stop() {
        streams.close();
        producer.close();
    }
}