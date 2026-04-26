package org.ww.wigglew.controller;

import org.springframework.web.bind.annotation.*;
import org.ww.wigglew.models.request.PasswordChangeRequest;
import org.ww.wigglew.models.response.AuthenticationResponse;
import org.ww.wigglew.models.request.LoginRequest;
import org.ww.wigglew.service.auth.AuthenticationService;

@RestController
@RequestMapping("/api/v1/admin/auth/")
public class AdminAuthenticationController {
   private final AuthenticationService authService;

    public AdminAuthenticationController(AuthenticationService service) {
        this.authService = service;
    }


    @PostMapping("/login")
    public AuthenticationResponse register(@RequestBody LoginRequest request){
        return authService.login(request, true); //adminCheck = true
    }

    @PostMapping("/change_password")
    public AuthenticationResponse changePassword(@RequestBody PasswordChangeRequest request){
        return authService.changePassword(request, true); //adminCheck = true
    }

}
