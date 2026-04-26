package org.ww.wigglew.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.ww.wigglew.entity.auth.Child;
import org.ww.wigglew.models.request.ChildRequest;
import org.ww.wigglew.models.response.AuthenticationResponse;
import org.ww.wigglew.service.ChildService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/child")
public class ChildController {

    @Autowired
    private ChildService childService;

    @PostMapping("/add")
    public ResponseEntity<AuthenticationResponse> addChild(
            @RequestHeader("Authorization") String token,
            @RequestBody ChildRequest childRequest)
    {
        AuthenticationResponse response = childService.addChild(token,
            childRequest.getName(), childRequest.getDob(), childRequest.getGender());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/toggle_active_session/{childId}")
    public ResponseEntity<AuthenticationResponse> toggleActiveSession(
            @RequestHeader("Authorization") String token,
            @PathVariable String childId) {
        AuthenticationResponse response = childService.toggleActiveSession(token, childId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Child>> listChildren(
            @RequestHeader("Authorization") String token) {

        List<Child> children = childService.getChildren(token);
        return ResponseEntity.ok(children);
    }

    @DeleteMapping("/delete/{childId}")
    public ResponseEntity<String> deleteChild(
            @RequestHeader("Authorization") String token,
            @PathVariable String childId) {

        childService.deleteChild(token, childId);
        return ResponseEntity.ok("Child deleted successfully");
    }


    @GetMapping("/active_child/")
    public ResponseEntity<?> getActiveChild(
            @RequestHeader("Authorization") String token) {
        String response = childService.getActiveChild(token);
        return ResponseEntity.ok(response);
    }
}
