package com.defender.service;

import com.defender.model.SmsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {
    @Mock
    private MongoService mongoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private KafkaService kafkaService;

    @BeforeEach
    void setUp() {
        kafkaService = new KafkaService(mongoService, objectMapper, true);
    }

    @Test
    void shouldStoreMessageDirectlyWhenReceiverNotSubscribed() {
        // given
        SmsMessage sms = new SmsMessage();
        sms.setSender("48111222333");
        sms.setReceiver("48444555666");
        sms.setMessage("Test message");

        when(mongoService.isSubscribed(sms.getReceiver())).thenReturn(false);

        // when
        kafkaService.sendMessage(sms);

        // then
        verify(mongoService).storeMessage(sms);
    }

    @Test
    void shouldHandleMongoError() {
        // given
        SmsMessage sms = new SmsMessage();
        sms.setSender("48111222333");
        sms.setReceiver("48444555666");
        sms.setMessage("Test message");

        when(mongoService.isSubscribed(sms.getReceiver())).thenReturn(false);
        doThrow(new RuntimeException("DB Error")).when(mongoService).storeMessage(any());

        // when/then
        assertThrows(RuntimeException.class, () -> kafkaService.sendMessage(sms));
    }
}