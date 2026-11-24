package com.library.service;

import com.library.dto.Dtos.*;
import com.library.exception.ApiException;
import com.library.model.Role;
import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        String username = r.username().trim();
        String email = r.email().trim().toLowerCase();
        if (users.existsByUsername(username))
            throw new ApiException(HttpStatus.CONFLICT, "Username is already taken");
        if (users.existsByEmail(email))
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(r.password()));
        u.setRole(Role.USER);
        users.save(u);
        return new AuthResponse(jwt.generate(u.getUsername()), u.getId(), u.getUsername(), u.getRole().name());
    }

    public AuthResponse login(LoginRequest r) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(r.username().trim(), r.password()));
        User u = users.findByUsername(r.username().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        return new AuthResponse(jwt.generate(u.getUsername()), u.getId(), u.getUsername(), u.getRole().name());
    }
}
