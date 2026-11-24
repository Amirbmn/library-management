package com.library.controller;

import com.library.dto.Dtos.*;
import com.library.security.AppUserDetails;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        return authService.register(r);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return authService.login(r);
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal AppUserDetails me) {
        return new AuthResponse(null, me.getId(), me.getUsername(), me.getRole());
    }
}
