package org.ww.wigglew.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.ww.wigglew.config.jwt.JWTExtractorService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CallService {

    private final JWTExtractorService jwtExtractorService;

    public CallService(JWTExtractorService jwtExtractorService) {
        this.jwtExtractorService = jwtExtractorService;
    }

    @Value("${stream.api.key:}")
    private String streamApiKey;

    @Value("${stream.api.secret:}")
    private String streamApiSecret;

    @Value("${stream.api.base-url:https://video.stream-io-api.com}")
    private String streamBaseUrl;

    @Value("${stream.user.token-validity-seconds:86400}")
    private long tokenValiditySeconds;

    private SecretKey getStreamSigningKey() {
        return Keys.hmacShaKeyFor(streamApiSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, Object> createUserTokenResponse(String authHeader) {
        String appToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        String phone = jwtExtractorService.extractUsername(appToken);
        String userId = normalizeUserId(phone);
        String name = "User " + userId;

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(tokenValiditySeconds);

        String streamToken = Jwts.builder()
                .claim("user_id", userId)
                .setSubject("user/" + userId)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(exp))
                .signWith(getStreamSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        Map<String, Object> payload = new HashMap<>();
        payload.put("apiKey", streamApiKey);
        payload.put("userId", userId);
        payload.put("userName", name);
        payload.put("token", streamToken);
        payload.put("expiresAt", exp.toString());
        return payload;
    }

    public Map<String, Object> getOrCreateCall(String authHeader, String requestedCallId) {
        String appToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;
        String phone = jwtExtractorService.extractUsername(appToken);
        String userId = normalizeUserId(phone);
        String userName = "User " + userId;
        String callId = (requestedCallId == null || requestedCallId.isBlank())
                ? "autimate-" + UUID.randomUUID().toString().substring(0, 10)
                : requestedCallId;

        String serverToken = Jwts.builder()
            .claim("user_id", userId)
            .setSubject("user/" + userId)
                .setIssuedAt(java.util.Date.from(Instant.now()))
                .setExpiration(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .signWith(getStreamSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        Map<String, Object> data = new HashMap<>();
        data.put("created_by_id", userId);

        Map<String, Object> member = new HashMap<>();
        member.put("user_id", userId);
        member.put("role", "admin");
        data.put("members", List.of(member));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

        WebClient client = WebClient.builder()
                .baseUrl(streamBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serverToken)
                .defaultHeader("Stream-Auth-Type", "jwt")
                .build();

        try {
            ensureUserExists(client, userId, userName);

            Map response = client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/video/call/default/{id}")
                            .queryParam("api_key", streamApiKey)
                            .build(callId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> payload = new HashMap<>();
            payload.put("callId", callId);
            payload.put("callType", "default");
            payload.put("call", response);
            return payload;
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Stream call creation failed: " + ex.getResponseBodyAsString());
        }
    }

    private void ensureUserExists(WebClient client, String userId, String userName) {
        Map<String, Object> oneUser = new HashMap<>();
        oneUser.put("id", userId);
        oneUser.put("name", userName);
        oneUser.put("role", "user");

        Map<String, Object> users = new HashMap<>();
        users.put(userId, oneUser);

        Map<String, Object> body = new HashMap<>();
        body.put("users", users);

        client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("api_key", streamApiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    private String normalizeUserId(String input) {
        if (input == null || input.isBlank()) {
            return "user_anon";
        }
        String cleaned = input.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (cleaned.length() > 64) {
            return cleaned.substring(0, 64);
        }
        return cleaned;
    }
}
