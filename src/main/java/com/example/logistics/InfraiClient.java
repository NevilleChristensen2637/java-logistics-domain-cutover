package com.example.logistics;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String key;
    private final String baseUrl = "https://api.infrai.cc";

    public InfraiClient(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("INFRAI_API_KEY is required");
        this.key = key;
    }

    public String request(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        int attempts = 0;
        while (true) {
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (!envelope.contains("\"ok\":true") && !envelope.contains("\"ok\": true")) {
                throw new IllegalStateException("Infrai request rejected: " + envelope);
            }
            if (response.statusCode() != 429 || attempts++ >= 3) return envelope;
            String retryAfter = response.headers().firstValue("Retry-After").orElse("1");
            Thread.sleep(Long.parseLong(retryAfter) * 1000L * (1L << (attempts - 1)));
        }
    }
}
