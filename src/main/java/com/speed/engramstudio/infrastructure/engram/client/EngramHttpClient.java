package com.speed.engramstudio.infrastructure.engram.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class EngramHttpClient {
    
    private final HttpClient httpClient;
    private final URI baseUri;
    private final int timeoutMs;
    
    public EngramHttpClient(String baseUrl, int timeoutMs) {
        this.baseUri = URI.create(baseUrl);
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .build();
    }
    
    public CompletableFuture<String> get(String path) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(baseUri.resolve(path))
            .timeout(Duration.ofMillis(timeoutMs))
            .header("Accept", "application/json")
            .GET()
            .build();
        
        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(this::validateResponse)
            .thenApply(HttpResponse::body);
    }
    
    public CompletableFuture<String> post(String path) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(baseUri.resolve(path))
            .timeout(Duration.ofMillis(timeoutMs))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        
        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(this::validateResponse)
            .thenApply(HttpResponse::body);
    }
    
    public CompletableFuture<String> post(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(baseUri.resolve(path))
            .timeout(Duration.ofMillis(timeoutMs))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(this::validateResponse)
            .thenApply(HttpResponse::body);
    }
    
    public CompletableFuture<String> delete(String path) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(baseUri.resolve(path))
            .timeout(Duration.ofMillis(timeoutMs))
            .DELETE()
            .build();
        
        return httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(this::validateResponse)
            .thenApply(HttpResponse::body);
    }
    
    private HttpResponse<String> validateResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return response;
        }
        
        throw new EngramApiException(
            "HTTP " + statusCode + ": " + response.body(),
            statusCode
        );
    }
    
    public String getBaseUrl() {
        return baseUri.toString();
    }
}