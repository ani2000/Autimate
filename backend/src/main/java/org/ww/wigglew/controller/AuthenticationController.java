package org.ww.wigglew.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ww.wigglew.models.request.PasswordChangeRequest;
import org.ww.wigglew.models.response.AuthenticationResponse;
import org.ww.wigglew.service.auth.AuthenticationService;
import org.ww.wigglew.models.request.LoginRequest;
import org.ww.wigglew.models.request.PasswordResetRequest;
import org.ww.wigglew.models.request.RegisterRequest;
import org.ww.wigglew.service.auth.SmsSenderService;

@RestController
@RequestMapping("/api/v1/auth/")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authenticationService.login(request, false)); //adminCheck = false.
    }

    @GetMapping("/send/otp/{receiver}")
    public ResponseEntity<String> sendOtp(@PathVariable String receiver){
        return authenticationService.sendOTP(receiver);
    }

    @GetMapping("/verify/{receiver}/{code}")
    public AuthenticationResponse verifyOtp(@PathVariable String receiver, @PathVariable String code){
        return authenticationService.verifyOTP(receiver, code);
    }

    @PostMapping("/reset_password")
    public AuthenticationResponse changePassword(@RequestBody PasswordResetRequest request){
        return authenticationService.resetPassword(request); //adminCheck = true
    }

    @PostMapping("/change_password")
    public AuthenticationResponse changePassword(@RequestBody PasswordChangeRequest request){
        return authenticationService.changePassword(request, false); //adminCheck = true
    }
}
