package com.defender.util;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class UrlExtractorTest {
    private final UrlExtractor extractor = new UrlExtractor();

    @Test
    void shouldReturnEmptyForNullInput() {
        assertTrue(extractor.extractUrl(null).isEmpty());
    }

    @Test
    void shouldReturnEmptyForMessageWithoutUrl() {
        assertTrue(extractor.extractUrl("Hello world").isEmpty());
    }

    @Test
    void shouldExtractHttpUrl() {
        Optional<String> result = extractor.extractUrl("Check http://example.com");
        assertTrue(result.isPresent());
        assertEquals("http://example.com", result.get());
    }

    @Test
    void shouldExtractHttpsUrl() {
        Optional<String> result = extractor.extractUrl("Check https://example.com");
        assertTrue(result.isPresent());
        assertEquals("https://example.com", result.get());
    }
}