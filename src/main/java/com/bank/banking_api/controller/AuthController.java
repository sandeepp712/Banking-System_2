package com.bank.banking_api.controller;

import com.bank.banking_api.domain.AccountRole;
import com.bank.banking_api.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        AccountRole role;

        try {
            role = AccountRole.valueOf(request.role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role. Allowed: " + java.util.Arrays.toString(AccountRole.values()));
        }

        authService.register(request.username, request.password, role, java.time.Instant.now());

        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.username, request.password);
        return ResponseEntity.ok(new LoginResponse(token));
    }


    //DTO (Records)
    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String role
    ) { }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) { }

    public record LoginResponse(String token) { }
}