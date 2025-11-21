package com.defender.service;

import com.defender.config.ConfigProvider;
import com.defender.model.SmsMessage;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MongoService {
    private final MongoClient mongoClient;
    private final MongoCollection<Document> subscribers;
    private final MongoCollection<Document> messages;
    private final ObjectMapper objectMapper;

    public MongoService(MongoClient mongoClient, ObjectMapper objectMapper) {

        this.mongoClient = mongoClient;

        MongoDatabase mongoDatabase = mongoClient.getDatabase(ConfigProvider.getMongoDatabase());
        this.subscribers = mongoDatabase.getCollection("subscribers");
        this.messages = mongoDatabase.getCollection("messages");
        this.objectMapper = objectMapper;

        // Create indexes
        subscribers.createIndex(Indexes.ascending("phoneNumber"));
        messages.createIndex(Indexes.ascending("receiver"));
    }

    public boolean isSubscribed(String phoneNumber) {
        return subscribers.countDocuments(
            Filters.eq("phoneNumber", phoneNumber)
        ) > 0;
    }

    public void subscribe(String phoneNumber) {
        if (!isSubscribed(phoneNumber)) {
            subscribers.insertOne(new Document("phoneNumber", phoneNumber));
        }
    }

    public void unsubscribe(String phoneNumber) {
        subscribers.deleteOne(Filters.eq("phoneNumber", phoneNumber));
    }

    public void storeMessage(SmsMessage sms) {
        try {
            Document doc = Document.parse(objectMapper.writeValueAsString(sms));
            doc.append("timestamp", System.currentTimeMillis());
            messages.insertOne(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store message", e);
        }
    }

    public void close() {
        mongoClient.close();
    }
}