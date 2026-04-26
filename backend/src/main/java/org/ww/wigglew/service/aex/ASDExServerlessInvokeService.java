package org.ww.wigglew.service.aex;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ww.wigglew.models.request.AsdExRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ASDExServerlessInvokeService {

    public String invokeServerless(String urlString, AsdExRequest requestPayload) {
        try {
            // Convert the request object to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(requestPayload);

            // Create URI
            URI uri = new URI(urlString);

            // Build the POST request with the Authorization header and JSON body
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // Send request asynchronously and handle timeout after 2 seconds
            CompletableFuture<Void> _future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .orTimeout(2, TimeUnit.SECONDS)
                    .thenAcceptAsync(response -> {
                        System.out.println("Response received asynchronously: " + response.body());
                    }).exceptionally(ex -> {
                        System.out.println("Request failed or timed out: " + ex.getMessage());
                        return null;
                    });

            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
