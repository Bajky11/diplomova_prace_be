package com.friends.friends.Controller;

import com.friends.friends.Entity.Account.*;
import com.friends.friends.Services.AccountService;
import com.friends.friends.Services.AuthService;
import com.friends.friends.Services.BrevoEmailService;
import com.friends.friends.Services.JwtResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private BrevoEmailService brevoEmailService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private JwtResetTokenService jwtResetTokenService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody AccountRegisterDto registerDto) {
        LoginResponse response = authService.register(registerDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody AccountLoginDto loginDto) {
        LoginResponse response = authService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestResetPassword(@RequestBody String email) {
        try {
            Account account = accountService.getAccountByEmail(email.trim());
            String token = jwtResetTokenService.issue(account.getId());
            brevoEmailService.sendPasswordRequestResetEmail(account.getEmail(), token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/reset")
    public ResponseEntity<?> resetPassword(@RequestBody AccountResetPasswordDto dto) {
        try {
            authService.resetPassword(dto.getToken(), dto.getNewPassword());
            return ResponseEntity.ok().body(Map.of("message", "Password updated"));
        } catch (JwtResetTokenService.JwtValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}