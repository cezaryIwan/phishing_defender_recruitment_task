package com.defender.service;

import com.defender.model.SmsMessage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoServiceTest {
    @Mock
    private MongoClient mongoClient;

    @Mock
    private MongoDatabase database;

    @Mock
    private MongoCollection<Document> subscribers;

    @Mock
    private MongoCollection<Document> messages;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MongoService mongoService;

    @BeforeEach
    void setUp() {
        when(mongoClient.getDatabase(any())).thenReturn(database);
        when(database.getCollection("subscribers")).thenReturn(subscribers);
        when(database.getCollection("messages")).thenReturn(messages);
        when(subscribers.createIndex(any())).thenReturn("index");
        when(messages.createIndex(any())).thenReturn("index");

        mongoService = new MongoService(mongoClient, objectMapper);
    }

    @Test
    void shouldSubscribeNewNumber() {
        // given
        String phoneNumber = "48123456789";
        when(subscribers.countDocuments(any(Bson.class))).thenReturn(0L);

        // when
        mongoService.subscribe(phoneNumber);

        // then
        verify(subscribers).insertOne(any(Document.class));
    }

    @Test
    void shouldNotSubscribeExistingNumber() {
        // given
        String phoneNumber = "48123456789";
        when(subscribers.countDocuments(any(Bson.class))).thenReturn(1L);

        // when
        mongoService.subscribe(phoneNumber);

        // then
        verify(subscribers, never()).insertOne(any());
    }

    @Test
    void shouldUnsubscribeNumber() {
        // given
        String phoneNumber = "48123456789";
        when(subscribers.deleteOne(any(Bson.class))).thenReturn(mock(DeleteResult.class));

        // when
        mongoService.unsubscribe(phoneNumber);

        // then
        verify(subscribers).deleteOne(any(Bson.class));
    }

    @Test
    void shouldStoreMessage() throws Exception {
        // given
        SmsMessage sms = new SmsMessage();
        sms.setSender("48111222333");
        sms.setReceiver("48444555666");
        sms.setMessage("Test message");

        String json = "{\"sender\":\"48111222333\",\"receiver\":\"48444555666\",\"message\":\"Test message\"}";

        // when
        mongoService.storeMessage(sms);

        // then
        verify(messages).insertOne(any(Document.class));
    }
}