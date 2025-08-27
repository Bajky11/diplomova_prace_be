package com.friends.friends.unit;

import com.friends.friends.Entity.Account.*;
import com.friends.friends.Exception.Account.AccountAlreadyExistsException;
import com.friends.friends.Exception.Auth.InvalidCredentialsException;
import com.friends.friends.Repository.AccountRepository;
import com.friends.friends.Services.AccountService;
import com.friends.friends.Services.AresService;
import com.friends.friends.Services.AuthService;
import com.friends.friends.Services.JwtResetTokenService;
import com.friends.friends.Util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock JwtUtil jwtUtil;
    @Mock
    AccountService accountService;
    @Mock
    JwtResetTokenService jwtResetTokenService;
    @Mock
    AresService aresService; // bude injektován přes ReflectionTestUtils

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void injectAres() {
        // AuthService vytváří new AresService(); přepíšeme ji mockem
        ReflectionTestUtils.setField(authService, "aresService", aresService);
    }

    // ------------ REGISTER ------------

    @Test
    void register_success_withoutIco() {
        AccountRegisterDto dto = new AccountRegisterDto();
        dto.setEmail("user@example.com");
        dto.setPassword("Secret123");
        dto.setName("User");
        dto.setIsBusiness(false);
        dto.setRegion("CZ");
        dto.setImageUrl("img");
        dto.setIco(null); // bez IČO

        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        Account saved = Account.builder()
                .id(1L)
                .email("user@example.com")
                .name("User")
                .passwordHash("$2a$10$anything") // skutečný hash si vytvoří service; zde není podstatné
                .isBusiness(false)
                .region("CZ")
                .imageUrl("img")
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(saved);
        when(jwtUtil.generateToken("user@example.com")).thenReturn("jwt-token");
        // aresService se nevolá, protože ICO je null

        LoginResponse resp = authService.register(dto);

        assertEquals("jwt-token", resp.getToken());
        assertNotNull(resp.getUser());
        assertEquals("user@example.com", resp.getUser().getEmail());
        verify(accountRepository).findByEmail("user@example.com");
        verify(accountRepository).save(any(Account.class));
        verify(jwtUtil).generateToken("user@example.com");
        verifyNoInteractions(aresService);
    }

    @Test
    void register_success_withValidIco() {
        AccountRegisterDto dto = new AccountRegisterDto();
        dto.setEmail("biz@example.com");
        dto.setPassword("Secret123");
        dto.setName("Biz");
        dto.setIsBusiness(true);
        dto.setRegion("CZ");
        dto.setIco(" 12345678 "); // se zarážkou, service trimuje

        when(accountRepository.findByEmail("biz@example.com")).thenReturn(Optional.empty());
        when(accountRepository.existsByIco("12345678")).thenReturn(false);
        when(aresService.checkIfIcoIsValid("12345678")).thenReturn(true);

        Account saved = Account.builder()
                .id(2L).email("biz@example.com").name("Biz")
                .passwordHash("$2a$10$anything").isBusiness(true).region("CZ").ico("12345678")
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(saved);
        when(jwtUtil.generateToken("biz@example.com")).thenReturn("jwt-token-2");

        LoginResponse resp = authService.register(dto);

        assertEquals("jwt-token-2", resp.getToken());
        assertEquals("biz@example.com", resp.getUser().getEmail());
        verify(aresService).checkIfIcoIsValid("12345678");
        verify(accountRepository).existsByIco("12345678");
    }

    @Test
    void register_fails_whenEmailExists() {
        AccountRegisterDto dto = new AccountRegisterDto();
        dto.setEmail("dup@example.com");
        dto.setPassword("x");

        when(accountRepository.findByEmail("dup@example.com"))
                .thenReturn(Optional.of(new Account()));

        AccountAlreadyExistsException ex = assertThrows(
                AccountAlreadyExistsException.class,
                () -> authService.register(dto)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));

        verify(accountRepository, never()).save(any());
        verifyNoInteractions(jwtUtil, aresService);
    }

    @Test
    void register_fails_whenIcoExists() {
        AccountRegisterDto dto = new AccountRegisterDto();
        dto.setEmail("new@example.com");
        dto.setPassword("x");
        dto.setIco("12345678");

        when(accountRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(accountRepository.existsByIco("12345678")).thenReturn(true);

        AccountAlreadyExistsException ex = assertThrows(
                AccountAlreadyExistsException.class,
                () -> authService.register(dto)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("ico"));

        verify(accountRepository, never()).save(any());
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(aresService); // k ověření IČO nedojde
    }

    @Test
    void register_fails_whenIcoInvalidByAres() {
        AccountRegisterDto dto = new AccountRegisterDto();
        dto.setEmail("new@example.com");
        dto.setPassword("x");
        dto.setIco("12345678");

        when(accountRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(accountRepository.existsByIco("12345678")).thenReturn(false);
        when(aresService.checkIfIcoIsValid("12345678")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.register(dto)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("ico"));

        verify(accountRepository, never()).save(any());
        verifyNoInteractions(jwtUtil);
    }

    // ------------ LOGIN ------------

    @Test
    void login_success() {
        // uložený uživatel s bcrypt hashem
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("Secret123");

        Account user = Account.builder()
                .id(3L).email("login@example.com").passwordHash(hash)
                .build();

        when(accountRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("login@example.com")).thenReturn("login-jwt");

        AccountLoginDto dto = new AccountLoginDto();
        dto.setEmail("login@example.com");
        dto.setPassword("Secret123");

        LoginResponse resp = authService.login(dto);

        assertEquals("login-jwt", resp.getToken());
        assertEquals("login@example.com", resp.getUser().getEmail());
        verify(jwtUtil).generateToken("login@example.com");
    }

    @Test
    void login_fails_whenWrongPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Account user = Account.builder()
                .id(4L).email("login@example.com").passwordHash(encoder.encode("CorrectPass"))
                .build();

        when(accountRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));

        AccountLoginDto dto = new AccountLoginDto();
        dto.setEmail("login@example.com");
        dto.setPassword("WrongPass");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(dto));
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_fails_whenEmailNotFound() {
        when(accountRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        AccountLoginDto dto = new AccountLoginDto();
        dto.setEmail("missing@example.com");
        dto.setPassword("x");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(dto));
        verifyNoInteractions(jwtUtil);
    }

    // ------------ RESET PASSWORD ------------

    @Test
    void resetPassword_hashesAndSaves() {
        when(jwtResetTokenService.verifyAndGetUserId("reset-token")).thenReturn(42L);

        Account acc = Account.builder()
                .id(42L).email("user@example.com").passwordHash("$2a$10$old")
                .build();

        when(accountService.getAccountById(42L)).thenReturn(acc);

        authService.resetPassword("reset-token", "NewSecret");

        // ověř, že hash se změnil (a je skutečně bcrypt hash odpovídající vstupu)
        String newHash = acc.getPasswordHash();
        assertNotNull(newHash);
        assertNotEquals("$2a$10$old", newHash);
        assertTrue(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .matches("NewSecret", newHash));

        verify(accountRepository).save(acc);
    }
}