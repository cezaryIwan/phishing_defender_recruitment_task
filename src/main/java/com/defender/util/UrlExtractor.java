package com.defender.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlExtractor {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]*"
    );

    public Optional<String> extractUrl(String text) {
        if (text == null) {
            return Optional.empty();
        }

        Matcher matcher = URL_PATTERN.matcher(text);
        return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
    }
}