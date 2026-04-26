package org.ww.wigglew.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ww.wigglew.models.request.StreamCallRequest;
import org.ww.wigglew.service.CallService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/call")
public class CallController {

    @Autowired
    private CallService callService;

    @GetMapping("/token")
    public ResponseEntity<?> createStreamToken(@RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> response = callService.createUserTokenResponse(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/get-or-create")
    public ResponseEntity<?> getOrCreateCall(
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) StreamCallRequest request) {
        try {
            String callId = request != null ? request.getCallId() : null;
            Map<String, Object> response = callService.getOrCreateCall(token, callId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
