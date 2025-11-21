package com.defender.service;

import com.defender.model.SmsMessage;
import com.defender.util.UrlExtractor;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
public class PhishingDetectorService {
    private final HttpClient httpClient;
    private final UrlExtractor urlExtractor;
    private static final String GOOGLE_API_URL = "https://cloud.google.com/web-risk/docs/reference/rest/v1eap1/TopLevel/evaluateUri";
    private static final String API_KEY = "examplary-api-key";

    public boolean isPhishing(SmsMessage message) {
        return urlExtractor.extractUrl(message.getMessage())
                .map(this::checkUrlForPhishing)
                .orElse(false);
    }

    private boolean checkUrlForPhishing(String url) {
        try {
            String requestBody = String.format("{\"uri\": \"%s\"}", url);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GOOGLE_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            return response.body().contains("MALICIOUS");
        } catch (Exception e) {
            return false;
        }
    }
}