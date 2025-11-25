package com.defender.service;

import com.defender.model.SmsMessage;
import com.defender.util.UrlExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhishingDetectorServiceTest {
    @Mock
    private HttpClient httpClient;

    @Mock
    private UrlExtractor urlExtractor;

    @Mock
    private HttpResponse<String> httpResponse;

    private PhishingDetectorService service;

    @BeforeEach
    void setUp() {
        service = new PhishingDetectorService(httpClient, urlExtractor);
    }

    @Test
    void shouldReturnFalseForMessageWithoutUrl() {
        // given
        SmsMessage message = new SmsMessage();
        message.setMessage("Hello world");
        when(urlExtractor.extractUrl("Hello world")).thenReturn(Optional.empty());

        // when
        boolean result = service.isPhishing(message);

        // then
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForPhishingUrl() throws Exception {
        // given
        SmsMessage message = new SmsMessage();
        message.setMessage("Check this: https://malicious-site.com");
        when(urlExtractor.extractUrl(message.getMessage()))
                .thenReturn(Optional.of("https://malicious-site.com"));
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("MALICIOUS");

        // when
        boolean result = service.isPhishing(message);

        // then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForSafeUrl() throws Exception {
        // given
        SmsMessage message = new SmsMessage();
        message.setMessage("Check this: https://safe-site.com");
        when(urlExtractor.extractUrl(message.getMessage()))
                .thenReturn(Optional.of("https://safe-site.com"));
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("SAFE");

        // when
        boolean result = service.isPhishing(message);

        // then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenApiCallFails() throws Exception {
        // given
        SmsMessage message = new SmsMessage();
        message.setMessage("Check this: https://some-site.com");
        when(urlExtractor.extractUrl(message.getMessage()))
                .thenReturn(Optional.of("https://some-site.com"));
        when(httpClient.send(any(), any())).thenThrow(new RuntimeException("API Error"));

        // when
        boolean result = service.isPhishing(message);

        // then
        assertFalse(result);
    }
}